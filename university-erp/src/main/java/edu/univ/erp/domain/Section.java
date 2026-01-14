package edu.univ.erp.domain;

import java.time.LocalDate;

public class Section {
    private int sectionId;
    private int courseId;
    private int instructorId;
    private String sectionCode;
    private String dayTime;
    private String room;
    private int capacity;
    private String semester;
    private int year;
    private int enrollmentCount;
    private LocalDate dropDeadline;
    private LocalDate addDeadline;

    private String courseCode;
    private String courseTitle;
    private String instructorName;
    
    public Section(int sectionId, int courseId, int instructorId, String sectionCode, 
                   String dayTime, String room, int capacity, String semester, int year, int enrollmentCount) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.sectionCode = sectionCode;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
        this.enrollmentCount = enrollmentCount;
    }
    
    public int getSectionId() { return sectionId; }
    public int getCourseId() { return courseId; }
    public int getInstructorId() { return instructorId; }
    public String getSectionCode() { return sectionCode; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }
    public int getEnrollmentCount() { return enrollmentCount; }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public String getInstructorName() { return instructorName; }
    
    public LocalDate getDropDeadline() { return dropDeadline; }
    public LocalDate getAddDeadline() { return addDeadline; }

    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public void setDropDeadline(LocalDate dropDeadline) { this.dropDeadline = dropDeadline; }
    public void setAddDeadline(LocalDate addDeadline) { this.addDeadline = addDeadline; }
    
    public boolean hasAvailableSeats() {
        return enrollmentCount < capacity;
    }
    
    public int getAvailableSeats() {
        return capacity - enrollmentCount;
    }
    
    @Override
    public String toString() {
        return String.format("%s - Section %s (%s)", courseCode, sectionCode, semester + " " + year);
    }
}