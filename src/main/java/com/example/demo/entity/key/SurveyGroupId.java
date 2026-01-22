package com.example.demo.entity.key;

import java.io.Serializable;

public class SurveyGroupId implements Serializable {
    private Long survey;
    private Long group;

    // Default constructor
    public SurveyGroupId() {

    }
    public SurveyGroupId(Long survey, Long group) {
        this.survey = survey;
        this.group = group;
    }
    // Getters and Setters
    public Long getSurvey() {
        return survey;
    }   
    public void setSurvey(Long survey) {
        this.survey = survey;
    }
    public Long getGroup() {
        return group;
    }
    public void setGroup(Long group) {
        this.group = group;
    }
}
