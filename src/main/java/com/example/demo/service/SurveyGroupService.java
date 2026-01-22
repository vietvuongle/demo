package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.repository.SurveyGroupRepository;

@Service
public class SurveyGroupService {

    @Autowired
    private SurveyGroupRepository surveyGroupRepository;

    public void deleteBySurveyIdAndGroupId(Long surveyId, Long groupId) {
    surveyGroupRepository.deleteBySurveyIdAndGroupId(surveyId, groupId);
}
    
}
