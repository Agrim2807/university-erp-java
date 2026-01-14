package edu.univ.erp.domain;

public class Instructor {
    private int userId;
    private String department;
    private String office;
    
    public Instructor(int userId, String department, String office) {
        this.userId = userId;
        this.department = department;
        this.office = office;
    }
    
    public int getUserId() { return userId; }
    public String getDepartment() { return department; }
    public String getOffice() { return office; }
    
    public void setDepartment(String department) { this.department = department; }
    public void setOffice(String office) { this.office = office; }
}