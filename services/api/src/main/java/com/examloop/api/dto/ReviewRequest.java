package com.examloop.api.dto;

import jakarta.validation.constraints.NotNull;

public class ReviewRequest {
    @NotNull(message = "Correct flag is required")
    private Boolean correct;

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }
}
