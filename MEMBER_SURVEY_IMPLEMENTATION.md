# Tài Liệu Hệ Thống Làm Khảo Sát Cho Thành Viên

## Tổng Quan

Hệ thống cho phép các thành viên (Member) xem danh sách các khảo sát có sẵn và tiến hành làm khảo sát. Các tính năng bao gồm:

- **Dashboard**: Hiển thị danh sách khảo sát chưa hoàn thành và đã hoàn thành
- **Làm Khảo Sát**: Form để trả lời các câu hỏi
- **Xem Chi Tiết**: Xem lại các câu trả lời của khảo sát đã hoàn thành

---

## Kiến Trúc

### Controller: MemberController

**Vị trí**: `src/main/java/com/example/demo/controller/MemberController.java`

#### Các Endpoint:

1. **GET /member/dashboard**
    - Hiển thị bảng điều khiển thành viên
    - Hiển thị: danh sách khảo sát chưa hoàn thành, khảo sát đã hoàn thành, thống kê
    - Model attributes:
        - `user`: Thông tin người dùng hiện tại
        - `pendingSurveys`: Danh sách khảo sát chưa hoàn thành
        - `completedSurveys`: Danh sách khảo sát đã hoàn thành
        - `totalSurveys`: Tổng số khảo sát
        - `completedCount`: Số khảo sát đã hoàn thành

2. **GET /member/survey/{surveyId}**
    - Hiển thị biểu mẫu khảo sát để trả lời
    - Kiểm tra:
        - Khảo sát có tồn tại không
        - Khảo sát đã hết hạn không
        - Người dùng đã hoàn thành khảo sát chưa
    - Model attributes:
        - `survey`: Thông tin khảo sát
        - `groupedQuestions`: Câu hỏi được nhóm theo QuestionGroup
        - `user`: Thông tin người dùng

3. **POST /member/submit/{surveyId}**
    - Xử lý việc gửi khảo sát
    - Xác thực:
        - Câu hỏi bắt buộc có câu trả lời
        - Người dùng chưa hoàn thành khảo sát này trước đó
    - Lưu SurveySubmission và SurveyAnswer

4. **GET /member/submission/{submissionId}**
    - Xem chi tiết khảo sát đã hoàn thành
    - Kiểm tra quyền truy cập (chỉ chủ sở hữu submission mới có thể xem)

---

## Repository

### SurveySubmissionRepository

**Vị trí**: `src/main/java/com/example/demo/repository/SurveySubmissionRepository.java`

#### Các Method:

```java
// Lấy danh sách khảo sát có sẵn cho người dùng
List<Survey> findAvailableSurveysForUser(Long userId);

// Lấy danh sách submission cho một khảo sát
List<SurveySubmission> findBySurveyId(Long surveyId);

// Lấy danh sách submission của người dùng
List<SurveySubmission> findByUserId(Long userId);

// Kiểm tra xem người dùng đã hoàn thành khảo sát chưa
Optional<SurveySubmission> findBySurveyIdAndUserId(Long surveyId, Long userId);
```

---

## Templates

### 1. member/dashboard.html

**Vị trí**: `src/main/resources/templates/member/dashboard.html`

**Tính năng**:

- Hiển thị header với thông tin người dùng
- Hiển thị thống kê (Tổng khảo sát, Đã hoàn thành)
- Danh sách khảo sát chưa hoàn thành (với nút "Làm Khảo Sát")
- Danh sách khảo sát đã hoàn thành (với nút "Xem Chi Tiết")
- Responsive design
- Xử lý alert messages

**Styles**:

- Gradient background cho header
- Card-based layout cho survey items
- Status badges (Chưa hoàn thành, Đã hoàn thành)
- Hover effects và animations

### 2. member/survey-form.html

**Vị trí**: `src/main/resources/templates/member/survey-form.html`

**Tính năng**:

- Form để trả lời câu hỏi khảo sát
- Hỗ trợ nhiều loại câu hỏi:
    - SHORT_TEXT: Input text (max 500 ký tự)
    - LONG_TEXT: Textarea (max 5000 ký tự)
    - SINGLE_CHOICE: Radio buttons
    - MULTIPLE_CHOICE: Checkboxes
    - DROPDOWN: Select dropdown
    - RATING: Rating scale
- Progress bar hiển thị tiến độ hoàn thành
- Validation cho câu hỏi bắt buộc (required)
- Loading spinner khi gửi form
- Thích nghi responsive

**Xử lý Form**:

```javascript
// Tất cả câu trả lời được gửi với key format: question_{questionId}
// Ví dụ: question_1, question_2, etc.

// Controller sẽ:
1. Lấy tất cả parameters từ form
2. Kiểm tra câu hỏi bắt buộc
3. Tạo SurveySubmission
4. Lưu các SurveyAnswer
```

### 3. member/submission-detail.html

**Vị trí**: `src/main/resources/templates/member/submission-detail.html`

**Tính năng**:

- Hiển thị chi tiết khảo sát đã hoàn thành
- Hiển thị:
    - Tiêu đề khảo sát
    - Status badge (Đã hoàn thành)
    - Thời gian hoàn thành
    - Tổng số câu hỏi
    - Toàn bộ câu hỏi và câu trả lời
- Chỉ cho phép xem nếu là chủ sở hữu submission

---

## Flow Diagram

```
┌─────────────────────────────────────────────┐
│         Thành Viên Truy Cập                  │
└────────────────┬────────────────────────────┘
                 │
                 ▼
        ┌────────────────┐
        │ GET /dashboard │
        └────────┬───────┘
                 │
    ┌────────────┴──────────────┐
    ▼                           ▼
┌─────────────┐        ┌─────────────────────┐
│ Khảo Sát    │        │ Khảo Sát Đã        │
│ Chưa Hoàn   │        │ Hoàn Thành         │
│ Thành       │        │                     │
└────┬────────┘        └─────────────────────┘
     │
     ▼
┌──────────────────────┐
│ GET /survey/{id}     │
│ Kiểm tra:            │
│ - Khảo sát tồn tại   │
│ - Chưa hết hạn       │
│ - Chưa hoàn thành    │
└──────────┬───────────┘
           │
           ▼
    ┌────────────────┐
    │ Survey Form    │
    │ (Trả lời)      │
    └────────┬───────┘
             │
             ▼
    ┌────────────────────┐
    │ POST /submit/{id}  │
    │ - Xác thực answers │
    │ - Lưu submission   │
    │ - Lưu answers      │
    └────────┬───────────┘
             │
             ▼
┌─────────────────────────┐
│ Redirect Dashboard      │
│ + Success Message       │
└─────────────────────────┘
```

---

## Entity Relationships

```
┌─────────────────┐
│ Survey          │
│ - id            │
│ - title         │
│ - description   │
│ - startDate     │
│ - endDate       │
│ - status        │
└────────┬────────┘
         │
         │ 1:N
         │
    ┌────┴──────────────────┐
    │                       │
    ▼                       ▼
┌─────────────┐    ┌──────────────────┐
│ SurveyGroup │    │ SurveySubmission │
│ - surveyId  │    │ - surveyId       │
│ - groupId   │    │ - userId         │
└────┬────────┘    │ - submittedAt    │
     │             └────────┬─────────┘
     │ 1:N                  │ 1:N
     │                      │
     ▼                      ▼
┌──────────────┐    ┌──────────────┐
│ QuestionGroup│    │ SurveyAnswer │
│ - id         │    │ - id         │
│ - name       │    │ - answerText │
└────┬─────────┘    │ - optionId   │
     │ 1:N          └──────────────┘
     │
     ▼
┌──────────────┐
│ Question     │
│ - id         │
│ - content    │
│ - type       │
│ - required   │
└────┬─────────┘
     │ 1:N
     │
     ▼
┌──────────────┐
│ QuestionOption
│ - id         │
│ - content    │
└──────────────┘
```

---

## Question Types

| Type            | Mô Tả                        | Form Element           |
| --------------- | ---------------------------- | ---------------------- |
| SHORT_TEXT      | Văn bản ngắn (max 500 ký tự) | Input text             |
| LONG_TEXT       | Văn bản dài (max 5000 ký tự) | Textarea               |
| SINGLE_CHOICE   | Chọn một lựa chọn            | Radio buttons          |
| MULTIPLE_CHOICE | Chọn nhiều lựa chọn          | Checkboxes             |
| DROPDOWN        | Chọn từ danh sách thả xuống  | Select dropdown        |
| RATING          | Đánh giá bằng thang điểm     | Radio buttons (rating) |

---

## Validation

### Backend Validation (MemberController)

1. **Khảo Sát Tồn Tại**

    ```java
    Survey survey = surveyRepository.findById(surveyId)
        .orElseThrow(() -> new RuntimeException("Khảo sát không tìm thấy"));
    ```

2. **Khảo Sát Chưa Hết Hạn**

    ```java
    if (survey.getEndDate() != null && survey.getEndDate().isBefore(LocalDateTime.now())) {
        return "redirect:/member/dashboard?error=survey_expired";
    }
    ```

3. **Chưa Hoàn Thành**

    ```java
    SurveySubmission existingSubmission = submissionRepository
        .findBySurveyIdAndUserId(surveyId, user.getId())
        .orElse(null);

    if (existingSubmission != null) {
        return "redirect:/member/dashboard?error=survey_already_completed";
    }
    ```

4. **Câu Hỏi Bắt Buộc**
    ```java
    if (question.isRequired() && (answerValue == null || answerValue.trim().isEmpty())) {
        submissionRepository.delete(savedSubmission);
        redirectAttributes.addFlashAttribute("error",
            "Vui lòng trả lời câu hỏi: " + question.getContent());
        return "redirect:/member/survey/" + surveyId;
    }
    ```

### Frontend Validation (HTML5)

- `required` attribute cho câu hỏi bắt buộc
- `maxlength` cho input text
- Validation messages khi gửi form

### JavaScript Validation

- Progress bar tracking
- Form submission handler
- Loading spinner

---

## Error Handling

| Lỗi                                     | Xử Lý                                       |
| --------------------------------------- | ------------------------------------------- |
| Khảo sát không tìm thấy                 | Throw RuntimeException                      |
| Khảo sát hết hạn                        | Redirect với error message                  |
| Đã hoàn thành                           | Redirect với error message                  |
| Câu hỏi bắt buộc thiếu                  | Delete submission, redirect với error       |
| Lỗi submit                              | Catch exception, redirect với error message |
| Unauthorized access (submission detail) | Redirect đến dashboard                      |

---

## Security

1. **Authentication**: Sử dụng `Authentication` object từ Spring Security
2. **Authorization**:
    - Chỉ có thể xem submission của chính mình
    - Chỉ có thể làm khảo sát được giao
3. **CSRF Protection**: Được xử lý bởi Spring Security mặc định

---

## Responsive Design

### Breakpoints:

- **Mobile** (< 768px): Responsive layout, stacked buttons
- **Tablet** (768px - 1024px): 2 columns cho statistics
- **Desktop** (> 1024px): Full layout, side-by-side layout

### Mobile Optimizations:

- Touch-friendly buttons
- Larger font sizes
- Flexible forms
- Mobile-friendly navbar

---

## Performance Considerations

1. **Database Queries**:
    - Sử dụng `EAGER` fetch cho `QuestionOption` trong `Question`
    - Sử dụng custom query cho `findAvailableSurveysForUser`

2. **Caching**:
    - Có thể cache danh sách khảo sát có sẵn

3. **Pagination**:
    - Có thể thêm pagination cho danh sách khảo sát

---

## Future Enhancements

1. **Progress Saving**: Lưu draft submission
2. **Time Tracking**: Theo dõi thời gian làm khảo sát
3. **Question Dependency**: Câu hỏi phụ thuộc vào câu trả lời trước
4. **File Upload**: Cho phép upload file cho một số câu hỏi
5. **Comments**: Cho phép thêm bình luận
6. **Export**: Xuất kết quả khảo sát thành PDF
7. **Analytics**: Thống kê chi tiết về kết quả khảo sát
8. **Notification**: Thông báo khi có khảo sát mới

---

## Testing

### Các Test Cases:

1. **Dashboard**
    - Load danh sách khảo sát
    - Lọc khảo sát chưa/đã hoàn thành
    - Xác thực counts

2. **Survey Form**
    - Load form với tất cả loại câu hỏi
    - Validate required fields
    - Submit form thành công
    - Handle duplicate submission

3. **Submission Detail**
    - Load submission
    - Xác thực ownership
    - Display answers correctly

---

## Troubleshooting

### Khảo sát không xuất hiện trong dashboard:

- Kiểm tra status = APPROVED
- Kiểm tra startDate/endDate
- Kiểm tra SurveyMember assignment

### Không thể gửi form:

- Kiểm tra validation errors
- Xác thực all required fields
- Kiểm tra browser console for JS errors

### Lỗi "Khảo sát không tìm thấy":

- Xác thực survey ID
- Kiểm tra database

---

## Reference

- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **Thymeleaf**: https://www.thymeleaf.org/
- **Bootstrap 5**: https://getbootstrap.com/
- **Font Awesome**: https://fontawesome.com/
