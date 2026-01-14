package edu.univ.erp.domain;

public class Course {
    private int courseId;
    private String code;
    private String title;
    private int credits;
    private String description;
    private boolean active;
    
    public Course(int courseId, String code, String title, int credits, String description, boolean active) {
        this.courseId = courseId;
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.description = description;
        this.active = active;
    }
    
    public int getCourseId() { return courseId; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCredits() { return credits; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    
    public void setCode(String code) { this.code = code; }
    public void setTitle(String title) { this.title = title; }
    public void setCredits(int credits) { this.credits = credits; }
    public void setDescription(String description) { this.description = description; }
    public void setActive(boolean active) { this.active = active; }
    
    @Override
    public String toString() {
        return code + " - " + title;
    }
}