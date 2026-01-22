package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.SurveyGroup;
import com.example.demo.entity.key.SurveyGroupId;

import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface SurveyGroupRepository extends JpaRepository<SurveyGroup, SurveyGroupId> {
    List<SurveyGroup> findBySurveyId(Long surveyId);
    List<SurveyGroup> findByGroupId(Long groupId);

    @Transactional
    @Modifying
    @Query("""
        DELETE FROM SurveyGroup sg
        WHERE sg.survey.id = :surveyId
          AND sg.group.id = :groupId
    """)
    void deleteBySurveyIdAndGroupId(
            @Param("surveyId") Long surveyId,
            @Param("groupId") Long groupId
    );
}