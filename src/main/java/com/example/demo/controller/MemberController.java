package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Survey;
import com.example.demo.entity.SurveyAnswer;
import com.example.demo.entity.SurveySubmission;
import com.example.demo.entity.User;
import com.example.demo.repository.SurveyRepository;
import com.example.demo.service.SurveyService;
import com.example.demo.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/member")
public class MemberController {
    @Autowired
    private SurveyService surveyService;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        List<Survey> surveys = surveyService.getAvailableSurveysForUser(user);
        model.addAttribute("surveys", surveys);
        return "member/dashboard";
    }

    @GetMapping("/survey/{surveyId}")
    public String viewSurvey(@PathVariable Long surveyId, Model model, Authentication auth) {
        // Load questions based on user's role (filter by target_role)
        // For simplicity, assume load all, but filter in view or service
        return "member/survey-form";
    }

    @PostMapping("/submit/{surveyId}")
    public String submit(@PathVariable Long surveyId, @ModelAttribute List<SurveyAnswer> answers, Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        Survey survey = surveyRepository.findById(surveyId).orElseThrow();
        SurveySubmission submission = new SurveySubmission();
        submission.setSurvey(survey);
        submission.setUser(user);
        surveyService.submitSurvey(submission, answers);
        return "redirect:/member/dashboard";
    }
}
