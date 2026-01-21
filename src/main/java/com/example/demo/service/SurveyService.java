package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Question;
import com.example.demo.entity.QuestionGroup;
import com.example.demo.entity.QuestionOption;
import com.example.demo.entity.Survey;
import com.example.demo.entity.SurveyAnswer;
import com.example.demo.entity.SurveyAssignRule;
import com.example.demo.entity.SurveyGroup;
import com.example.demo.entity.SurveySubmission;
import com.example.demo.entity.User;
import com.example.demo.enums.SurveyStatus;
import com.example.demo.repository.QuestionGroupRepository;
import com.example.demo.repository.QuestionOptionRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.SurveyAnswerRepository;
import com.example.demo.repository.SurveyAssignRuleRepository;
import com.example.demo.repository.SurveyGroupRepository;
import com.example.demo.repository.SurveyRepository;
import com.example.demo.repository.SurveySubmissionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;

@Service
public class SurveyService {

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private SurveyAssignRuleRepository assignRuleRepository;

    @Autowired
    private QuestionGroupRepository questionGroupRepository;

    @Autowired
    private SurveyGroupRepository surveyGroupRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository optionRepository;

    @Autowired
    private SurveySubmissionRepository submissionRepository;

    @Autowired
    private SurveyAnswerRepository answerRepository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public Survey createSurvey(Survey survey, User createdBy) {
        survey.setCreatedBy(createdBy);
        survey.setCreatedAt(LocalDateTime.now());
        survey.setStatus(SurveyStatus.DRAFT);
        return surveyRepository.save(survey);
    }

    @Transactional
    public void addAssignRule(Long surveyId, SurveyAssignRule rule) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow();
        rule.setSurvey(survey);
        assignRuleRepository.save(rule);
    }

    @Transactional
    public QuestionGroup createOrUpdateQuestionGroup(QuestionGroup group) {
        return questionGroupRepository.save(group);
    }

    @Transactional
    public void addGroupToSurvey(Long surveyId, Long groupId) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow();
        QuestionGroup group = questionGroupRepository.findById(groupId).orElseThrow();
        SurveyGroup sg = new SurveyGroup();
        sg.setSurvey(survey);
        sg.setGroup(group);
        surveyGroupRepository.save(sg);
    }

    @Transactional
    public Question createQuestion(Question question, Long groupId) {
        QuestionGroup group = questionGroupRepository.findById(groupId).orElseThrow();
        question.setGroup(group);
        return questionRepository.save(question);
    }

    @Transactional
    public QuestionOption createOption(QuestionOption option, Long questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow();
        option.setQuestion(question);
        return optionRepository.save(option);
    }

    @Transactional
    public void setPending(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow();
        survey.setStatus(SurveyStatus.PENDING);
        surveyRepository.save(survey);
    }

    public List<Survey> getPendingSurveys() {
        return surveyRepository.findByStatus(SurveyStatus.PENDING);
    }

    public List<Survey> getApprovedSurveys() {
        return surveyRepository.findByStatus(SurveyStatus.APPROVED);
    }

    @Transactional
    public void approveSurvey(Long surveyId, User pm) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_approve_survey");
        query.registerStoredProcedureParameter("p_survey_id", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_pm_id", Long.class, ParameterMode.IN);
        query.setParameter("p_survey_id", surveyId);
        query.setParameter("p_pm_id", pm.getId());
        query.execute();
    }

    public List<Survey> getAvailableSurveysForUser(User user) {
        return submissionRepository.findAvailableSurveysForUser(user.getId());
    }

    public List<Survey> getAllSurveys() {
        return surveyRepository.findAll();
    }

    public Survey getSurveyById(Long id) {
        return surveyRepository.findById(id).orElseThrow(() -> new RuntimeException("Survey not found"));
    }

    @Transactional
    public Survey updateSurvey(Long id, Survey surveyUpdate) {
        Survey survey = surveyRepository.findById(id).orElseThrow(() -> new RuntimeException("Survey not found"));
        if (surveyUpdate.getTitle() != null) {
            survey.setTitle(surveyUpdate.getTitle());
        }
        if (surveyUpdate.getDescription() != null) {
            survey.setDescription(surveyUpdate.getDescription());
        }
        if (surveyUpdate.getStartDate() != null) {
            survey.setStartDate(surveyUpdate.getStartDate());
        }
        if (surveyUpdate.getEndDate() != null) {
            survey.setEndDate(surveyUpdate.getEndDate());
        }
        return surveyRepository.save(survey);
    }

    @Transactional
    public SurveySubmission submitSurvey(SurveySubmission submission, List<SurveyAnswer> answers) {
        submission.setSubmittedAt(LocalDateTime.now());
        SurveySubmission saved = submissionRepository.save(submission);
        for (SurveyAnswer ans : answers) {
            ans.setSubmission(saved);
            answerRepository.save(ans);
        }
        return saved;
    }

    // Reporting methods...
    public List<SurveySubmission> getSubmissionsForSurvey(Long surveyId) {
        return submissionRepository.findBySurveyId(surveyId);
    }

    public List<SurveyGroup> getSurveyGroups(Long surveyId) {
        return surveyGroupRepository.findBySurveyId(surveyId);
    }

    public Question getQuestionById(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow();
    }

    public Long getSurveyIdByQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow();
        QuestionGroup group = question.getGroup();
        // Get survey from survey_group
        List<SurveyGroup> surveyGroups = surveyGroupRepository.findByGroupId(group.getId());
        if (!surveyGroups.isEmpty()) {
            return surveyGroups.get(0).getSurvey().getId();
        }
        throw new RuntimeException("Survey not found for question");
    }

    // Other methods for list, update, delete...
}
