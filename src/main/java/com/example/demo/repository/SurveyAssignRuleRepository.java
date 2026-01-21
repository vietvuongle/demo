package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.SurveyAssignRule;

@Repository
public interface SurveyAssignRuleRepository extends JpaRepository<SurveyAssignRule, Long> {
}
