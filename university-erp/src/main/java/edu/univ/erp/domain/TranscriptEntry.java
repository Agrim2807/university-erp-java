package edu.univ.erp.domain;

import java.util.ArrayList;
import java.util.List;

public class TranscriptEntry {
    private String courseCode;
    private String courseTitle;
    private int credits;
    private String semester;
    private int year;
    private String finalGrade;
    private List<String> componentGrades; 

    public TranscriptEntry(String courseCode, String courseTitle, int credits, String semester, int year, String finalGrade) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.credits = credits;
        this.semester = semester;
        this.year = year;
        this.finalGrade = finalGrade;
        this.componentGrades = new ArrayList<>();
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public int getCredits() { return credits; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }
    public String getFinalGrade() { return (finalGrade != null) ? finalGrade : "IP"; }
    public List<String> getComponentGrades() { return componentGrades; }
    public void setComponentGrades(List<String> componentGrades) { this.componentGrades = componentGrades; }

    public boolean hasComponentGrades() {
        return componentGrades != null && !componentGrades.isEmpty();
    }

    public String getComponentGradesAsString() {
        if (!hasComponentGrades()) {
            return "No grades entered yet";
        }
        return String.join("; ", componentGrades);
    }
}
