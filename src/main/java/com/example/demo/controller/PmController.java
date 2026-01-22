package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.Survey;
import com.example.demo.entity.SurveySubmission;
import com.example.demo.entity.User;
import com.example.demo.service.SurveyService;
import com.example.demo.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/pm")
public class PmController {
    @Autowired
    private SurveyService surveyService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pendingSurveys", surveyService.getPendingSurveys());
        model.addAttribute("approvedSurveys", surveyService.getApprovedSurveys());
        return "pm/dashboard";
    }

    // View survey detail
    @GetMapping("/survey/{surveyId}")
    public String viewSurveyDetail(@PathVariable Long surveyId, Model model) {
        Survey survey = surveyService.getSurveyById(surveyId);
        model.addAttribute("survey", survey);
        model.addAttribute("surveyGroups", surveyService.getSurveyGroups(surveyId));
        return "pm/survey-detail";
    }

    @PostMapping("/approve/{surveyId}")
    public String approve(@PathVariable Long surveyId, Authentication auth) {
        try {
            User pm = userService.findByUsername(auth.getName());
            if (pm == null) {
                System.err.println("PM user not found: " + auth.getName());
                return "redirect:/pm/dashboard";
            }
            surveyService.approveSurvey(surveyId, pm);
            return "redirect:/pm/dashboard";
        } catch (Exception e) {
            System.err.println("Error approving survey: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/pm/dashboard";
        }
    }

    @PostMapping("/reject/{surveyId}")
    public String reject(@PathVariable Long surveyId) {
        surveyService.rejectSurvey(surveyId);
        return "redirect:/pm/dashboard";
    }

    // View reports
    @GetMapping("/report/{surveyId}")
    public String report(@PathVariable Long surveyId, Model model) {
        List<SurveySubmission> submissions = surveyService.getSubmissionsForSurvey(surveyId);
        model.addAttribute("submissions", submissions);
        model.addAttribute("survey", surveyService.getSurveyById(surveyId));
        return "pm/report";
    }
}
