package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.*;
import com.example.demo.service.SurveyService;
import com.example.demo.service.UserService;
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

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("survey", new Survey());
        model.addAttribute("surveys", surveyService.getAllSurveys());
        return "admin/dashboard";
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
        return "admin/surveys/detail";
    }

    @PostMapping("/survey/{surveyId}/add-group/{groupId}")
    public String addGroupToSurvey(@PathVariable Long surveyId, @PathVariable Long groupId) {
        surveyService.addGroupToSurvey(surveyId, groupId);
        return "redirect:/admin/survey-detail/" + surveyId;
    }

    @PostMapping("/survey/{surveyId}/create-group")
    public String createAndAddGroup(@PathVariable Long surveyId, @ModelAttribute QuestionGroup group) {
        QuestionGroup created = surveyService.createOrUpdateQuestionGroup(group);
        surveyService.addGroupToSurvey(surveyId, created.getId());
        return "redirect:/admin/survey/" + surveyId + "/questions";
    }

    @GetMapping("/survey/{surveyId}/questions")
    public String surveyQuestions(@PathVariable Long surveyId, Model model) {
        Survey survey = surveyService.getSurveyById(surveyId);
        model.addAttribute("survey", survey);
        model.addAttribute("surveyGroups", surveyService.getSurveyGroups(surveyId));
        model.addAttribute("newQuestion", new Question());
        return "admin/surveys/questions";
    }

    @PostMapping("/survey/{surveyId}/question")
    public String addQuestion(@PathVariable Long surveyId, @ModelAttribute Question question) {
        Long groupId = question.getGroup().getId();
        surveyService.createQuestion(question, groupId);
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
