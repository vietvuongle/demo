package com.example.demo.repository;

import com.example.demo.entity.Survey;
import com.example.demo.enums.SurveyStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository

public interface SurveyRepository extends JpaRepository<Survey, Long> {
        Optional<Survey> findById(Long id);
        
        List<Survey> findByStatus(SurveyStatus status);

}

