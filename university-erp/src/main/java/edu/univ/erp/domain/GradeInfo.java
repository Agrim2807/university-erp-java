package edu.univ.erp.domain;

public class GradeInfo {
    private String courseCode;
    private String sectionCode;
    private String componentName;
    private double score;
    private double maxScore;
    private double weight;
    private String finalGrade;

    public GradeInfo(String courseCode, String sectionCode, String componentName, double score, double maxScore, double weight, String finalGrade) {
        this.courseCode = courseCode;
        this.sectionCode = sectionCode;
        this.componentName = componentName;
        this.score = score;
        this.maxScore = maxScore;
        this.weight = weight;
        this.finalGrade = finalGrade;
    }

    public String getCourseCode() { return courseCode; }
    public String getSectionCode() { return sectionCode; }
    public String getComponentName() { return componentName; }
    public double getScore() { return score; }
    public double getMaxScore() { return maxScore; }
    public double getWeight() { return weight; }
    public String getFinalGrade() { return finalGrade; }
}