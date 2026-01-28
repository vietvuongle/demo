package com.example.demo.repository;

import com.example.demo.entity.Survey;
import com.example.demo.entity.SurveySubmission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveySubmissionRepository extends JpaRepository<SurveySubmission, Long> {
    @Query("SELECT s FROM Survey s JOIN SurveyMember sm ON sm.survey = s WHERE sm.user.id = :userId AND s.status = 'APPROVED' AND CURRENT_TIMESTAMP BETWEEN s.startDate AND s.endDate AND NOT EXISTS (SELECT ss FROM SurveySubmission ss WHERE ss.survey = s AND ss.user.id = :userId) ORDER BY s.startDate DESC")
    List<Survey> findAvailableSurveysForUser(Long userId);

    List<SurveySubmission> findBySurveyId(Long surveyId);

    List<SurveySubmission> findByUserId(Long userId);

    Optional<SurveySubmission> findBySurveyIdAndUserId(@Param("surveyId") Long surveyId, @Param("userId") Long userId);
}
