package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.SurveyGroup;
import com.example.demo.entity.key.SurveyGroupId;
import java.util.List;

@Repository
public interface SurveyGroupRepository extends JpaRepository<SurveyGroup, SurveyGroupId> {
    List<SurveyGroup> findBySurveyId(Long surveyId);
    List<SurveyGroup> findByGroupId(Long groupId);
}