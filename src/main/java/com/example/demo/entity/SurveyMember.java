package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import com.example.demo.entity.key.SurveyMemberId;

import java.time.LocalDateTime;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "survey_member")
public class SurveyMember {

    @EmbeddedId
    private SurveyMemberId id;

    @ManyToOne
    @MapsId("surveyId")
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime assignedAt;
}

