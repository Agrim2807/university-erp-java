package edu.univ.erp.domain;

import java.util.HashMap;
import java.util.Map;

public class StudentGradeEntry {
    private int studentId;
    private int enrollmentId;
    private String studentName;
    private String rollNo;
    private String finalGrade;
    private Map<Integer, Double> scores; 

    public StudentGradeEntry(int studentId, int enrollmentId, String studentName, String rollNo, String finalGrade) {
        this.studentId = studentId;
        this.enrollmentId = enrollmentId;
        this.studentName = studentName;
        this.rollNo = rollNo;
        this.finalGrade = finalGrade;
        this.scores = new HashMap<>();
    }
    public int getStudentId() { return studentId; }
    public int getEnrollmentId() { return enrollmentId; }
    public String getStudentName() { return studentName; }
    public String getRollNo() { return rollNo; }
    public String getFinalGrade() { return finalGrade; }
    public Map<Integer, Double> getScores() { return scores; }

    public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }
    public void addScore(int componentId, double score) {
        this.scores.put(componentId, score);
    }
}