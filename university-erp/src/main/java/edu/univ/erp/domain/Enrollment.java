package edu.univ.erp.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Enrollment {
    private int enrollmentId;
    private int studentId;
    private int sectionId;
    private String status;
    private LocalDateTime enrolledAt;
    private LocalDateTime droppedAt;

    private String studentName;
    private String sectionInfo;
    private String courseCode;
    private LocalDate dropDeadline;
    
    public Enrollment(int enrollmentId, int studentId, int sectionId, String status, 
                      LocalDateTime enrolledAt, LocalDateTime droppedAt) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
        this.enrolledAt = enrolledAt;
        this.droppedAt = droppedAt;
    }
    
    public int getEnrollmentId() { return enrollmentId; }
    public int getStudentId() { return studentId; }
    public int getSectionId() { return sectionId; }
    public String getStatus() { return status; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public LocalDateTime getDroppedAt() { return droppedAt; }
    public String getStudentName() { return studentName; }
    public String getSectionInfo() { return sectionInfo; }
    public String getCourseCode() { return courseCode; }
    public LocalDate getDropDeadline() { return dropDeadline; }

    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setSectionInfo(String sectionInfo) { this.sectionInfo = sectionInfo; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setDropDeadline(LocalDate dropDeadline) { this.dropDeadline = dropDeadline; }
    
    public boolean isActive() {
        return "registered".equals(status);
    }
}