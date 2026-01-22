package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.*;
import com.example.demo.service.SurveyGroupService;
import com.example.demo.service.SurveyService;
import com.example.demo.service.UserService;
import com.example.demo.enums.RoleCode;
import com.example.demo.enums.RuleType;
import com.example.demo.repository.QuestionGroupRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private SurveyService surveyService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionGroupRepository questionGroupRepository;

    @Autowired
    private SurveyGroupService surveyGroupService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        // Statistics
        long totalSurveys = surveyService.getAllSurveys().size();
        long pendingSurveys = surveyService.getPendingSurveys().size();
        long approvedSurveys = surveyService.getApprovedSurveys().size();
        long totalUsers = userService.getAllUsers().size();
        
        model.addAttribute("totalSurveys", totalSurveys);
        model.addAttribute("pendingSurveys", pendingSurveys);
        model.addAttribute("approvedSurveys", approvedSurveys);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("surveys", surveyService.getAllSurveys());
        
        return "admin/dashboard";
    }

    @GetMapping("/survey-detail/create")
    public String createSurveyForm(Model model) {
        model.addAttribute("survey", new Survey());
        return "admin/create-survey";
    }

    @PostMapping("/create-survey")
    public String createSurvey(@ModelAttribute Survey survey, Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        Survey created = surveyService.createSurvey(survey, user);
        return "redirect:/admin/survey-detail/" + created.getId();
    }

    @GetMapping("/survey-detail/{id}")
    public String surveyDetail(@PathVariable Long id, Model model) {
        Survey survey = surveyService.getSurveyById(id);
        model.addAttribute("survey", survey);
        model.addAttribute("surveyGroups", surveyService.getSurveyGroups(id));
        model.addAttribute("allGroups", questionGroupRepository.findAll());
        model.addAttribute("assignRules", surveyService.getAssignRules(id));
        return "admin/detail";
    }

    @PostMapping("/survey/{surveyId}/assign-rule")
    public String addAssignRule(@PathVariable Long surveyId, 
                                @RequestParam String ruleType,
                                @RequestParam(required = false) String ruleValue,
                                RedirectAttributes redirectAttributes) {
        SurveyAssignRule rule = new SurveyAssignRule();
        Survey survey = surveyService.getSurveyById(surveyId);
        rule.setSurvey(survey);
        rule.setRuleType(RuleType.valueOf(ruleType));
        rule.setRuleValue(ruleValue);
        surveyService.addAssignRule(surveyId, rule);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm quy tắc phân công thành công!");
        return "redirect:/admin/survey-detail/" + surveyId;
    }

    @PostMapping("/survey/{surveyId}/group/{groupId}/delete")
    public String removeGroupFromSurvey(@PathVariable Long surveyId,
                                        @PathVariable Long groupId,
                                        RedirectAttributes redirectAttributes) {
        surveyGroupService.deleteBySurveyIdAndGroupId(surveyId, groupId);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa nhóm khỏi survey thành công!");

        return "redirect:/admin/survey-detail/" + surveyId;
    }


    @PostMapping("/survey/{surveyId}/add-group/{groupId}")
    public String addGroupToSurvey(@PathVariable Long surveyId, @PathVariable Long groupId,
                                   RedirectAttributes redirectAttributes) {
        surveyService.addGroupToSurvey(surveyId, groupId);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm nhóm câu hỏi thành công!");
        return "redirect:/admin/survey-detail/" + surveyId;
    }

    @PostMapping("/survey/{surveyId}/create-group")
    public String createAndAddGroup(@PathVariable Long surveyId, @ModelAttribute QuestionGroup group,
                                    RedirectAttributes redirectAttributes) {
        QuestionGroup created = surveyService.createOrUpdateQuestionGroup(group);
        surveyService.addGroupToSurvey(surveyId, created.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Tạo và thêm nhóm câu hỏi thành công!");
        return "redirect:/admin/survey-detail/" + surveyId;
    }

    @GetMapping("/survey/{surveyId}/questions")
    public String surveyQuestions(@PathVariable Long surveyId, Model model) {
        Survey survey = surveyService.getSurveyById(surveyId);
        model.addAttribute("survey", survey);
        model.addAttribute("surveyGroups", surveyService.getSurveyGroups(surveyId));
        model.addAttribute("newQuestion", new Question());
        return "admin/questions";
    }

    @PostMapping("/survey/{surveyId}/question")
    public String addQuestion(@PathVariable Long surveyId, @ModelAttribute Question question, String optionsText) {
        Long groupId = question.getGroup().getId();
        Question createdQuestion = surveyService.createQuestion(question, groupId);
        
        // If question type is OPTION and optionsText is provided, create options
        if ("OPTION".equals(question.getType().toString()) && optionsText != null && !optionsText.trim().isEmpty()) {
            String[] optionLines = optionsText.split("\\n");
            for (String line : optionLines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    QuestionOption option = new QuestionOption();
                    option.setContent(trimmedLine);
                    option.setQuestion(createdQuestion);
                    surveyService.createOption(option, createdQuestion.getId());
                }
            }
        }
        
        return "redirect:/admin/survey/" + surveyId + "/questions";
    }

    @PostMapping("/question/{questionId}/option")
    public String addQuestionOption(@PathVariable Long questionId, @ModelAttribute QuestionOption option) {
        Question question = surveyService.getQuestionById(questionId);
        Long surveyId = surveyService.getSurveyIdByQuestion(questionId);
        option.setQuestion(question);
        surveyService.createOption(option, questionId);
        return "redirect:/admin/survey/" + surveyId + "/questions";
    }

    @PostMapping("/add-rule/{surveyId}")
    public String addRule(@PathVariable Long surveyId, @ModelAttribute SurveyAssignRule rule) {
        surveyService.addAssignRule(surveyId, rule);
        return "redirect:/admin/edit-survey/" + surveyId;
    }

    @GetMapping("/edit-survey/{id}")
    public String editSurvey(@PathVariable Long id, Model model) {
        Survey survey = surveyService.getSurveyById(id);
        model.addAttribute("survey", survey);
        return "admin/edit";
    }

    // --- User management (ADMIN) ---
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new com.example.demo.entity.User());
        model.addAttribute("roles", EnumSet.allOf(RoleCode.class));
        return "admin/create-user";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute com.example.demo.entity.User user, String[] selectedRoles) {
        Set<RoleCode> roleCodes = EnumSet.noneOf(RoleCode.class);
        if (selectedRoles != null) {
            for (String r : selectedRoles) {
                try {
                    roleCodes.add(RoleCode.valueOf(r));
                } catch (Exception ex) {
                    // ignore invalid
                }
            }
        }
        if (roleCodes.isEmpty()) {
            roleCodes.add(RoleCode.MEMBER);
        }
        userService.createUser(user, roleCodes);
        return "redirect:/admin/users";
    }

    @PostMapping("/edit-survey/{id}")
    public String updateSurvey(@PathVariable Long id, @ModelAttribute Survey survey) {
        surveyService.updateSurvey(id, survey);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/set-pending/{id}")
    public String setPending(@PathVariable Long id) {
        surveyService.setPending(id);
        return "redirect:/admin/dashboard";
    }
}
