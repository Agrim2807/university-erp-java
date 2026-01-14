package edu.univ.erp;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.PasswordUtil;
import edu.univ.erp.domain.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthServiceTest {

    @Test
    @DisplayName("Test BCrypt password hashing")
    @Order(1)
    void testPasswordHashing() {
        String plainPassword = "testPassword123";
        String hash1 = PasswordUtil.hashPassword(plainPassword);
        String hash2 = PasswordUtil.hashPassword(plainPassword);

        
        assertNotEquals(hash1, hash2, "BCrypt should generate different hashes");

        
        assertTrue(PasswordUtil.verifyPassword(plainPassword, hash1));
        assertTrue(PasswordUtil.verifyPassword(plainPassword, hash2));
    }

    @Test
    @DisplayName("Test password verification with correct password")
    @Order(2)
    void testCorrectPasswordVerification() {
        String plainPassword = "correctPassword123";
        String hash = PasswordUtil.hashPassword(plainPassword);

        assertTrue(PasswordUtil.verifyPassword(plainPassword, hash),
                   "Correct password should verify successfully");
    }

    @Test
    @DisplayName("Test password verification with wrong password")
    @Order(3)
    void testWrongPassword() {
        String plainPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hash = PasswordUtil.hashPassword(plainPassword);

        assertFalse(PasswordUtil.verifyPassword(wrongPassword, hash),
                   "Wrong password should not verify");
    }

    @Test
    @DisplayName("Test BCrypt rounds (should be 12 for security)")
    @Order(4)
    void testBCryptRounds() {
        String password = "test123";
        String hash = PasswordUtil.hashPassword(password);

        
        
        String[] parts = hash.split("\\$");
        if (parts.length >= 3) {
            int rounds = Integer.parseInt(parts[2]);
            assertEquals(12, rounds, "BCrypt should use 12 rounds for security");
        } else {
            fail("Invalid BCrypt hash format");
        }
    }

    @Test
    @DisplayName("Test BCrypt hash format validity")
    @Order(5)
    void testBCryptHashFormat() {
        String password = "testPassword";
        String hash = PasswordUtil.hashPassword(password);

        
        assertTrue(hash.startsWith("$2a$"), "BCrypt hash should start with $2a$");
        assertEquals(60, hash.length(), "BCrypt hash should be 60 characters");
    }

    @Test
    @DisplayName("Test null password handling")
    @Order(6)
    void testNullPassword() {
        assertThrows(IllegalArgumentException.class,
                    () -> PasswordUtil.hashPassword(null),
                    "Null password should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Test empty password handling")
    @Order(7)
    void testEmptyPassword() {
        assertThrows(IllegalArgumentException.class,
                    () -> PasswordUtil.hashPassword(""),
                    "Empty password should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Test password verification with null hash")
    @Order(8)
    void testVerifyPasswordNullHash() {
        assertFalse(PasswordUtil.verifyPassword("password", null),
                   "Verification with null hash should return false");
    }

    @Test
    @DisplayName("Test password verification with empty hash")
    @Order(9)
    void testVerifyPasswordEmptyHash() {
        assertFalse(PasswordUtil.verifyPassword("password", ""),
                   "Verification with empty hash should return false");
    }

    @Test
    @DisplayName("Test password hashing is consistent")
    @Order(10)
    void testPasswordHashingConsistency() {
        String password = "mySecurePassword123!";
        String hash = PasswordUtil.hashPassword(password);

        
        assertTrue(PasswordUtil.verifyPassword(password, hash));
        assertTrue(PasswordUtil.verifyPassword(password, hash));
        assertTrue(PasswordUtil.verifyPassword(password, hash));
    }

    @Test
    @DisplayName("Test different passwords produce different hashes")
    @Order(11)
    void testDifferentPasswordsDifferentHashes() {
        String password1 = "password1";
        String password2 = "password2";

        String hash1 = PasswordUtil.hashPassword(password1);
        String hash2 = PasswordUtil.hashPassword(password2);

        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
        assertFalse(PasswordUtil.verifyPassword(password1, hash2), "Password1 should not verify against hash2");
        assertFalse(PasswordUtil.verifyPassword(password2, hash1), "Password2 should not verify against hash1");
    }

    @Test
    @DisplayName("Test BCrypt with special characters")
    @Order(12)
    void testBCryptWithSpecialCharacters() {
        String password = "P@ssw0rd!#$%&*()";
        String hash = PasswordUtil.hashPassword(password);

        assertTrue(PasswordUtil.verifyPassword(password, hash),
                   "Special characters should be handled correctly");
    }
}
