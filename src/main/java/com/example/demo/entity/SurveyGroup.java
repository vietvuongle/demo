package com.example.demo.entity;

import com.example.demo.entity.key.SurveyGroupId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "survey_group")
@IdClass(SurveyGroupId.class)
public class SurveyGroup {
    @Id
    @ManyToOne
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Id
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private QuestionGroup group;
}
