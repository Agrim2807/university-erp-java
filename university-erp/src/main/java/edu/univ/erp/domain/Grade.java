package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class Grade {
    private int gradeId;
    private int enrollmentId;
    private int componentId;
    private double score;
    private LocalDateTime enteredAt;

    public Grade(int gradeId, int enrollmentId, int componentId, double score, LocalDateTime enteredAt) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.componentId = componentId;
        this.score = score;
        this.enteredAt = enteredAt;
    }

    public int getGradeId() { return gradeId; }
    public int getEnrollmentId() { return enrollmentId; }
    public int getComponentId() { return componentId; }
    public double getScore() { return score; }
    public LocalDateTime getEnteredAt() { return enteredAt; }
}