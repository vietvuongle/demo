package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Question;
import com.example.demo.entity.QuestionGroup;
import com.example.demo.entity.Survey;
import com.example.demo.entity.SurveyAnswer;
import com.example.demo.entity.SurveyGroup;
import com.example.demo.entity.SurveySubmission;
import com.example.demo.entity.User;
import com.example.demo.repository.SurveyRepository;
import com.example.demo.repository.SurveySubmissionRepository;
import com.example.demo.service.SurveyService;
import com.example.demo.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/member")
public class MemberController {
    @Autowired
    private SurveyService surveyService;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private SurveySubmissionRepository submissionRepository;

    @Autowired
    private UserService userService;

    /**
     * Hiển thị bảng điều khiển thành viên với danh sách khảo sát
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        
        // Lấy danh sách khảo sát có sẵn cho người dùng
        List<Survey> availableSurveys = surveyService.getAvailableSurveysForUser(user);
        
        // Lấy danh sách khảo sát đã hoàn thành
        List<SurveySubmission> completedSurveys = submissionRepository.findByUserId(user.getId());
        List<Long> completedSurveyIds = completedSurveys.stream()
                .map(s -> s.getSurvey().getId())
                .collect(Collectors.toList());
        
        // Lọc khảo sát chưa hoàn thành
        List<Survey> pendingSurveys = availableSurveys.stream()
                .filter(s -> !completedSurveyIds.contains(s.getId()))
                .collect(Collectors.toList());
        
        model.addAttribute("user", user);
        model.addAttribute("pendingSurveys", pendingSurveys);
        model.addAttribute("completedSurveys", completedSurveys);
        model.addAttribute("totalSurveys", availableSurveys.size());
        model.addAttribute("completedCount", completedSurveys.size());
        
        return "member/dashboard";
    }

    /**
     * Hiển thị biểu mẫu khảo sát để trả lời
     */
    @GetMapping("/survey/{surveyId}")
    public String viewSurvey(@PathVariable Long surveyId, Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        
        // Lấy thông tin khảo sát
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Khảo sát không tìm thấy"));
        
        // Kiểm tra ngày hết hạn
        if (survey.getEndDate() != null && survey.getEndDate().isBefore(LocalDateTime.now())) {
            return "redirect:/member/dashboard?error=survey_expired";
        }
        
        // Kiểm tra xem người dùng đã hoàn thành khảo sát chưa
        SurveySubmission existingSubmission = submissionRepository
                .findBySurveyIdAndUserId(surveyId, user.getId())
                .orElse(null);
        
        if (existingSubmission != null) {
            return "redirect:/member/dashboard?error=survey_already_completed";
        }
        
        // Lấy danh sách nhóm câu hỏi cho khảo sát
        List<SurveyGroup> surveyGroups = surveyService.getSurveyGroups(surveyId);
        
        // Tổ chức câu hỏi theo nhóm
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

    /**
     * Xử lý việc gửi khảo sát
     */
    @PostMapping("/submit/{surveyId}")
    public String submit(@PathVariable Long surveyId, 
                         @RequestParam Map<String, String> params,
                         Authentication auth, 
                         RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(auth.getName());
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new RuntimeException("Khảo sát không tìm thấy"));
            
            // Kiểm tra xem người dùng đã hoàn thành khảo sát chưa
            if (submissionRepository.findBySurveyIdAndUserId(surveyId, user.getId()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Bạn đã hoàn thành khảo sát này rồi");
                return "redirect:/member/dashboard";
            }
            
            // Tạo đơn gửi khảo sát
            SurveySubmission submission = new SurveySubmission();
            submission.setSurvey(survey);
            submission.setUser(user);
            submission.setSubmittedAt(LocalDateTime.now());
            
            // Lưu đơn gửi trước
            SurveySubmission savedSubmission = submissionRepository.save(submission);
            
            // Xử lý câu trả lời
            List<SurveyAnswer> answers = new ArrayList<>();
            
            // Lấy tất cả câu hỏi từ khảo sát
            List<SurveyGroup> surveyGroups = surveyService.getSurveyGroups(surveyId);
            for (SurveyGroup sg : surveyGroups) {
                for (Question question : sg.getGroup().getQuestions()) {
                    String paramKey = "question_" + question.getId();
                    String answerValue = params.get(paramKey);
                    
                    // Kiểm tra yêu cầu bắt buộc
                    if (question.isRequired() && (answerValue == null || answerValue.trim().isEmpty())) {
                        submissionRepository.delete(savedSubmission);
                        redirectAttributes.addFlashAttribute("error", 
                            "Vui lòng trả lời câu hỏi: " + question.getContent());
                        return "redirect:/member/survey/" + surveyId;
                    }
                    
                    if (answerValue != null && !answerValue.trim().isEmpty()) {
                        SurveyAnswer answer = new SurveyAnswer();
                        answer.setSubmission(savedSubmission);
                        answer.setQuestion(question);
                        answer.setAnswerText(answerValue);
                        answers.add(answer);
                    }
                }
            }
            
            // Lưu khảo sát cùng với các câu trả lời
            surveyService.submitSurvey(savedSubmission, answers);
            
            redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã hoàn thành khảo sát!");
            return "redirect:/member/dashboard";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/member/survey/" + surveyId;
        }
    }

    /**
     * Xem chi tiết khảo sát đã hoàn thành
     */
    @GetMapping("/submission/{submissionId}")
    public String viewSubmission(@PathVariable Long submissionId, Model model, Authentication auth) {
        try {
            SurveySubmission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new RuntimeException("Đơn gửi không tìm thấy"));
            
            // Kiểm tra quyền truy cập
            User currentUser = userService.findByUsername(auth.getName());
            if (!submission.getUser().getId().equals(currentUser.getId())) {
                return "redirect:/member/dashboard";
            }
            
            // Lấy survey groups
            Survey survey = submission.getSurvey();
            List<SurveyGroup> surveyGroups = surveyService.getSurveyGroups(survey.getId());
            
            model.addAttribute("submission", submission);
            model.addAttribute("survey", survey);
            model.addAttribute("surveyGroups", surveyGroups);
            
            return "member/submission-detail";
        } catch (Exception e) {
            return "redirect:/member/dashboard";
        }
    }
}
