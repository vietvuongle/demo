# Hướng Dẫn Phát Triển - Mở Rộng Hệ Thống Khảo Sát Thành Viên

## I. Cấu Trúc Mã Nguồn

```
src/main/java/com/example/demo/
├── controller/
│   └── MemberController.java          # 210 lines
├── entity/
│   ├── Survey.java
│   ├── Question.java
│   ├── QuestionGroup.java
│   ├── SurveySubmission.java
│   ├── SurveyAnswer.java
│   └── ...
├── repository/
│   ├── SurveySubmissionRepository.java
│   └── ...
└── service/
    └── SurveyService.java

src/main/resources/templates/member/
├── dashboard.html                      # 320 lines
├── survey-form.html                    # 480 lines
└── submission-detail.html              # 320 lines
```

---

## II. Code Examples

### 1. MemberController - Dashboard Method

```java
@GetMapping("/dashboard")
public String dashboard(Model model, Authentication auth) {
    // Lấy người dùng hiện tại
    User user = userService.findByUsername(auth.getName());

    // Lấy tất cả khảo sát có sẵn
    List<Survey> availableSurveys = surveyService.getAvailableSurveysForUser(user);

    // Lấy submissions của người dùng
    List<SurveySubmission> completedSurveys = submissionRepository.findByUserId(user.getId());
    List<Long> completedSurveyIds = completedSurveys.stream()
            .map(s -> s.getSurvey().getId())
            .collect(Collectors.toList());

    // Lọc khảo sát chưa hoàn thành
    List<Survey> pendingSurveys = availableSurveys.stream()
            .filter(s -> !completedSurveyIds.contains(s.getId()))
            .collect(Collectors.toList());

    // Thêm vào model
    model.addAttribute("user", user);
    model.addAttribute("pendingSurveys", pendingSurveys);
    model.addAttribute("completedSurveys", completedSurveys);
    model.addAttribute("totalSurveys", availableSurveys.size());
    model.addAttribute("completedCount", completedSurveys.size());

    return "member/dashboard";
}
```

### 2. MemberController - Survey Form Method

```java
@GetMapping("/survey/{surveyId}")
public String viewSurvey(@PathVariable Long surveyId, Model model, Authentication auth) {
    User user = userService.findByUsername(auth.getName());

    // Tìm khảo sát
    Survey survey = surveyRepository.findById(surveyId)
            .orElseThrow(() -> new RuntimeException("Khảo sát không tìm thấy"));

    // Kiểm tra hết hạn
    if (survey.getEndDate() != null && survey.getEndDate().isBefore(LocalDateTime.now())) {
        return "redirect:/member/dashboard?error=survey_expired";
    }

    // Kiểm tra đã hoàn thành
    SurveySubmission existingSubmission = submissionRepository
            .findBySurveyIdAndUserId(surveyId, user.getId())
            .orElse(null);

    if (existingSubmission != null) {
        return "redirect:/member/dashboard?error=survey_already_completed";
    }

    // Lấy nhóm câu hỏi
    List<SurveyGroup> surveyGroups = surveyService.getSurveyGroups(surveyId);

    // Sắp xếp câu hỏi
    Map<QuestionGroup, List<Question>> groupedQuestions = new java.util.LinkedHashMap<>();
    for (SurveyGroup sg : surveyGroups) {
        QuestionGroup group = sg.getGroup();
        List<Question> questions = group.getQuestions().stream()
                .sorted((q1, q2) -> {
                    Integer order1 = q1.getOrderIndex() != null ? q1.getOrderIndex() : 0;
                    Integer order2 = q2.getOrderIndex() != null ? q2.getOrderIndex() : 0;
                    return order1.compareTo(order2);
                })
                .collect(Collectors.toList());
        groupedQuestions.put(group, questions);
    }

    model.addAttribute("survey", survey);
    model.addAttribute("groupedQuestions", groupedQuestions);
    model.addAttribute("user", user);

    return "member/survey-form";
}
```

### 3. MemberController - Submit Method

```java
@PostMapping("/submit/{surveyId}")
public String submit(@PathVariable Long surveyId,
                     @RequestParam Map<String, String> params,
                     Authentication auth,
                     RedirectAttributes redirectAttributes) {
    try {
        User user = userService.findByUsername(auth.getName());
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Khảo sát không tìm thấy"));

        // Kiểm tra đã hoàn thành
        if (submissionRepository.findBySurveyIdAndUserId(surveyId, user.getId()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Bạn đã hoàn thành khảo sát này rồi");
            return "redirect:/member/dashboard";
        }

        // Tạo submission
        SurveySubmission submission = new SurveySubmission();
        submission.setSurvey(survey);
        submission.setUser(user);
        submission.setSubmittedAt(LocalDateTime.now());

        // Lưu submission trước
        SurveySubmission savedSubmission = submissionRepository.save(submission);

        // Xử lý answers
        List<SurveyAnswer> answers = new ArrayList<>();
        List<SurveyGroup> surveyGroups = surveyService.getSurveyGroups(surveyId);

        for (SurveyGroup sg : surveyGroups) {
            for (Question question : sg.getGroup().getQuestions()) {
                String paramKey = "question_" + question.getId();
                String answerValue = params.get(paramKey);

                // Validate required
                if (question.isRequired() && (answerValue == null || answerValue.trim().isEmpty())) {
                    submissionRepository.delete(savedSubmission);
                    redirectAttributes.addFlashAttribute("error",
                        "Vui lòng trả lời câu hỏi: " + question.getContent());
                    return "redirect:/member/survey/" + surveyId;
                }

                // Lưu answer
                if (answerValue != null && !answerValue.trim().isEmpty()) {
                    SurveyAnswer answer = new SurveyAnswer();
                    answer.setSubmission(savedSubmission);
                    answer.setQuestion(question);
                    answer.setAnswerText(answerValue);
                    answers.add(answer);
                }
            }
        }

        // Lưu
        surveyService.submitSurvey(savedSubmission, answers);

        redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã hoàn thành khảo sát!");
        return "redirect:/member/dashboard";

    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        return "redirect:/member/survey/" + surveyId;
    }
}
```

### 4. Repository Methods

```java
// Lấy submissions của user
public List<SurveySubmission> findByUserId(Long userId) {
    return submissionRepository.findByUserId(userId);
}

// Kiểm tra submission của survey + user
public Optional<SurveySubmission> findBySurveyIdAndUserId(Long surveyId, Long userId) {
    return submissionRepository.findBySurveyIdAndUserId(surveyId, userId);
}
```

### 5. HTML Form Example (survey-form.html)

```html
<!-- Single Choice Question -->
<div th:if="${question.type == 'SINGLE_CHOICE'}" class="option-group">
    <div th:each="option : ${question.options}" class="radio-option">
        <input type="radio" th:id="'option_' + ${option.id}" th:name="'question_' + ${question.id}" th:value="${option.id}" th:required="${question.required}" class="form-check-input question-input" />
        <label th:for="'option_' + ${option.id}" class="form-check-label" th:text="${option.content}"></label>
    </div>
</div>

<!-- Text Input -->
<div th:if="${question.type == 'SHORT_TEXT'}">
    <input type="text" th:name="'question_' + ${question.id}" class="form-control form-control-custom" th:required="${question.required}" placeholder="Nhập câu trả lời của bạn" maxlength="500" />
</div>

<!-- Long Text -->
<div th:if="${question.type == 'LONG_TEXT'}">
    <textarea th:name="'question_' + ${question.id}" class="form-control form-control-custom" th:required="${question.required}" placeholder="Nhập câu trả lời của bạn (tối đa 5000 ký tự)"></textarea>
</div>
```

---

## III. Mở Rộng Tính Năng

### A. Thêm Loại Câu Hỏi Mới

**1. Định nghĩa enum**

```java
// QuestionType.java
public enum QuestionType {
    SHORT_TEXT,
    LONG_TEXT,
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    DROPDOWN,
    RATING,
    DATE,           // New
    FILE_UPLOAD,    // New
    MATRIX          // New
}
```

**2. Thêm form rendering**

```html
<!-- Date Input -->
<div th:if="${question.type == 'DATE'}">
    <input type="date" th:name="'question_' + ${question.id}" class="form-control form-control-custom" th:required="${question.required}" />
</div>

<!-- File Upload -->
<div th:if="${question.type == 'FILE_UPLOAD'}">
    <input type="file" th:name="'question_' + ${question.id}" class="form-control form-control-custom" accept=".pdf,.doc,.docx,.xlsx" th:required="${question.required}" />
</div>
```

**3. Xử lý trong controller**

```java
if (question.getType() == QuestionType.DATE) {
    // Handle date input
} else if (question.getType() == QuestionType.FILE_UPLOAD) {
    // Handle file upload
}
```

### B. Thêm Draft Saving

**1. Entity**

```java
@Entity
public class SurveyDraft {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Survey survey;

    @ManyToOne
    private User user;

    @OneToMany
    private List<SurveyAnswer> tempAnswers;

    private LocalDateTime lastSavedAt;
}
```

**2. Controller**

```java
@PostMapping("/save-draft/{surveyId}")
public ResponseEntity<?> saveDraft(@PathVariable Long surveyId,
                                    @RequestBody Map<String, String> answers,
                                    Authentication auth) {
    User user = userService.findByUsername(auth.getName());
    // Save draft logic
    return ResponseEntity.ok("Draft saved");
}

@GetMapping("/draft/{surveyId}")
public void loadDraft(@PathVariable Long surveyId, Model model, Authentication auth) {
    User user = userService.findByUsername(auth.getName());
    // Load draft logic
}
```

**3. HTML**

```html
<script>
    // Auto-save draft every 30 seconds
    setInterval(() => {
        const formData = new FormData(document.getElementById("surveyForm"));
        fetch(`/member/save-draft/{surveyId}`, {
            method: "POST",
            body: JSON.stringify(Object.fromEntries(formData)),
        });
    }, 30000);
</script>
```

### C. Thêm Time Tracking

**1. Entity**

```java
@Entity
public class SurveySubmission {
    // ... existing fields

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;

    public Long getTimeSpentSeconds() {
        return Duration.between(startedAt, submittedAt).getSeconds();
    }
}
```

**2. JavaScript**

```javascript
// Track time
const startTime = Date.now();

document.getElementById("surveyForm").addEventListener("submit", function () {
    const timeSpent = Math.floor((Date.now() - startTime) / 1000);
    const input = document.createElement("input");
    input.type = "hidden";
    input.name = "timeSpentSeconds";
    input.value = timeSpent;
    this.appendChild(input);
});
```

### D. Thêm Export PDF

**1. Dependency**

```xml
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>
```

**2. Service**

```java
@Service
public class ExportService {

    public byte[] exportSubmissionAsPDF(Long submissionId) throws Exception {
        SurveySubmission submission = submissionRepository.findById(submissionId).get();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdfDoc);

        // Add content
        document.add(new Paragraph(submission.getSurvey().getTitle())
            .setFontSize(20).setBold());

        for (SurveyAnswer answer : submission.getAnswers()) {
            document.add(new Paragraph(answer.getQuestion().getContent()));
            document.add(new Paragraph(answer.getAnswerText()));
        }

        document.close();
        return baos.toByteArray();
    }
}
```

**3. Controller**

```java
@GetMapping("/submission/{submissionId}/export")
public ResponseEntity<?> exportPDF(@PathVariable Long submissionId) {
    byte[] pdf = exportService.exportSubmissionAsPDF(submissionId);
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=survey.pdf")
        .body(pdf);
}
```

---

## IV. Testing

### Unit Tests

```java
@SpringBootTest
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SurveyService surveyService;

    @MockBean
    private UserService userService;

    @Test
    public void testDashboardLoads() throws Exception {
        mockMvc.perform(get("/member/dashboard")
            .principal(createMockAuthentication()))
            .andExpect(status().isOk())
            .andExpect(view().name("member/dashboard"));
    }

    @Test
    public void testSurveyFormLoads() throws Exception {
        mockMvc.perform(get("/member/survey/1")
            .principal(createMockAuthentication()))
            .andExpect(status().isOk());
    }

    @Test
    public void testSubmitValidation() throws Exception {
        // Test required field validation
    }

    private Authentication createMockAuthentication() {
        return new UsernamePasswordAuthenticationToken(
            "testuser", "password", Arrays.asList());
    }
}
```

### Integration Tests

```java
@SpringBootTest
public class SurveyIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testFullSurveyFlow() {
        // 1. Login
        // 2. Get dashboard
        // 3. Get survey form
        // 4. Submit survey
        // 5. Verify submission
    }
}
```

---

## V. Performance Optimization

### 1. Database Indexing

```sql
-- Improve query performance
ALTER TABLE survey_submission ADD INDEX idx_survey_user (survey_id, user_id);
ALTER TABLE survey_submission ADD INDEX idx_user_id (user_id);
ALTER TABLE survey_answer ADD INDEX idx_submission_id (submission_id);
```

### 2. Caching

```java
@Cacheable("available-surveys")
public List<Survey> getAvailableSurveysForUser(User user) {
    return submissionRepository.findAvailableSurveysForUser(user.getId());
}

@CacheEvict(value = "available-surveys", allEntries = true)
public void submitSurvey(SurveySubmission submission, List<SurveyAnswer> answers) {
    // ...
}
```

### 3. Pagination

```java
public Page<Survey> getAvailableSurveys(User user, Pageable pageable) {
    return surveyRepository.findByStatusAndEndDateAfter(
        SurveyStatus.APPROVED,
        LocalDateTime.now(),
        pageable
    );
}
```

---

## VI. Common Issues & Solutions

### Issue 1: Câu hỏi không hiển thị

```java
// Check:
1. SurveyStatus = APPROVED
2. QuestionGroup assigned to Survey via SurveyGroup
3. Questions exist in QuestionGroup
4. EAGER fetching enabled on options
```

### Issue 2: Submit bất thành công

```java
// Debug:
1. Check required field values
2. Verify form parameter names match "question_{id}"
3. Check database constraints
4. View error logs
```

### Issue 3: Performance slow

```java
// Optimize:
1. Add database indexes
2. Implement caching
3. Use pagination
4. Profile with Spring Actuator
```

---

## VII. Deployment

### Docker

```dockerfile
FROM openjdk:11
COPY target/demo-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Docker Compose

```yaml
version: "3"
services:
    app:
        build: .
        ports:
            - "8080:8080"
        environment:
            SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/survey_db
            SPRING_DATASOURCE_USERNAME: root
            SPRING_DATASOURCE_PASSWORD: password

    db:
        image: mysql:8.0
        environment:
            MYSQL_ROOT_PASSWORD: password
            MYSQL_DATABASE: survey_db
```

---

## VIII. Configuration Examples

### Application YAML

```yaml
spring:
    application:
        name: survey-system

    datasource:
        url: jdbc:mysql://localhost:3306/survey_db
        username: root
        password: password
        hikari:
            maximum-pool-size: 10

    jpa:
        hibernate:
            ddl-auto: validate
        show-sql: false
        properties:
            hibernate.format_sql: true

    mvc:
        view:
            prefix: /templates/
            suffix: .html

    cache:
        type: simple

logging:
    level:
        com.example.demo: DEBUG
        org.springframework.web: INFO
        org.hibernate: WARN

server:
    port: 8080
    servlet:
        context-path: /
```

---

**End of Development Guide**
Last Updated: January 24, 2026
