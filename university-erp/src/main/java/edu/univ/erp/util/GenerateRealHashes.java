package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;

public class GenerateRealHashes {
    public static void main(String[] args) {
        
        String adminHash = BCrypt.hashpw("admin123", BCrypt.gensalt());
        String instHash = BCrypt.hashpw("inst123", BCrypt.gensalt());
        String stuHash = BCrypt.hashpw("stu123", BCrypt.gensalt());
        
        System.out.println("=== REAL BCRYPT HASHES ===");
        System.out.println("Admin (admin123): " + adminHash);
        System.out.println("Instructor (inst123): " + instHash);
        System.out.println("Student (stu123): " + stuHash);
        System.out.println("=========================");
        
        
        System.out.println("Verification:");
        System.out.println("Admin verify: " + BCrypt.checkpw("admin123", adminHash));
        System.out.println("Instructor verify: " + BCrypt.checkpw("inst123", instHash));
        System.out.println("Student verify: " + BCrypt.checkpw("stu123", stuHash));
    }
}