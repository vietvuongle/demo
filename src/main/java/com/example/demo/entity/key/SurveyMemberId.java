package com.example.demo.entity.key;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Embeddable
public class SurveyMemberId implements Serializable {

    private Long surveyId;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SurveyMemberId that)) return false;
        return Objects.equals(surveyId, that.surveyId)
            && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(surveyId, userId);
    }
}

