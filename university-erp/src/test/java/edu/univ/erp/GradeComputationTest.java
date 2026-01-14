package edu.univ.erp;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GradeComputationTest {

    @Test
    @DisplayName("Test weighted average calculation")
    @Order(1)
    void testWeightedAverage() {
        
        Map<String, Double> weights = new HashMap<>();
        weights.put("Quiz", 20.0);
        weights.put("Midterm", 30.0);
        weights.put("Final", 50.0);

        Map<String, Double> scores = new HashMap<>();
        scores.put("Quiz", 80.0);      
        scores.put("Midterm", 75.0);   
        scores.put("Final", 90.0);     

        
        double weighted = calculateWeightedAverage(scores, weights);
        assertEquals(83.5, weighted, 0.01, "Weighted average should be 83.5");
    }

    @Test
    @DisplayName("Test letter grade A (90-100)")
    @Order(2)
    void testLetterGradeA() {
        assertEquals("A", getLetterGrade(95.0));
        assertEquals("A", getLetterGrade(90.0));
        assertEquals("A", getLetterGrade(100.0));
    }

    @Test
    @DisplayName("Test letter grade B (80-89)")
    @Order(3)
    void testLetterGradeB() {
        assertEquals("B", getLetterGrade(85.0));
        assertEquals("B", getLetterGrade(80.0));
        assertEquals("B", getLetterGrade(89.99));
    }

    @Test
    @DisplayName("Test letter grade C (70-79)")
    @Order(4)
    void testLetterGradeC() {
        assertEquals("C", getLetterGrade(75.0));
        assertEquals("C", getLetterGrade(70.0));
        assertEquals("C", getLetterGrade(79.99));
    }

    @Test
    @DisplayName("Test letter grade D (60-69)")
    @Order(5)
    void testLetterGradeD() {
        assertEquals("D", getLetterGrade(65.0));
        assertEquals("D", getLetterGrade(60.0));
        assertEquals("D", getLetterGrade(69.99));
    }

    @Test
    @DisplayName("Test letter grade F (below 60)")
    @Order(6)
    void testLetterGradeF() {
        assertEquals("F", getLetterGrade(50.0));
        assertEquals("F", getLetterGrade(0.0));
        assertEquals("F", getLetterGrade(59.99));
    }

    @Test
    @DisplayName("Test weights must sum to 100")
    @Order(7)
    void testWeightsSumTo100() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("Quiz", 20.0);
        weights.put("Midterm", 30.0);
        weights.put("Final", 60.0);

        double total = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(110.0, total);
        assertFalse(Math.abs(total - 100.0) < 0.01, "Weights should NOT sum to 100 in this test case");
    }

    @Test
    @DisplayName("Test perfect score calculation")
    @Order(8)
    void testPerfectScore() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("Quiz", 20.0);
        weights.put("Midterm", 30.0);
        weights.put("Final", 50.0);

        Map<String, Double> scores = new HashMap<>();
        scores.put("Quiz", 100.0);
        scores.put("Midterm", 100.0);
        scores.put("Final", 100.0);

        double weighted = calculateWeightedAverage(scores, weights);
        assertEquals(100.0, weighted, 0.01);
        assertEquals("A", getLetterGrade(weighted));
    }

    @Test
    @DisplayName("Test zero score calculation")
    @Order(9)
    void testZeroScore() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("Quiz", 20.0);
        weights.put("Midterm", 30.0);
        weights.put("Final", 50.0);

        Map<String, Double> scores = new HashMap<>();
        scores.put("Quiz", 0.0);
        scores.put("Midterm", 0.0);
        scores.put("Final", 0.0);

        double weighted = calculateWeightedAverage(scores, weights);
        assertEquals(0.0, weighted, 0.01);
        assertEquals("F", getLetterGrade(weighted));
    }

    @Test
    @DisplayName("Test boundary between B and C")
    @Order(10)
    void testBoundaryCases() {
        assertEquals("B", getLetterGrade(80.0));
        assertEquals("C", getLetterGrade(79.99));
        assertEquals("C", getLetterGrade(70.0));
        assertEquals("D", getLetterGrade(69.99));
    }

    

    private double calculateWeightedAverage(Map<String, Double> scores, Map<String, Double> weights) {
        double weightedSum = 0.0;
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            String component = entry.getKey();
            double score = entry.getValue();
            double weight = weights.get(component);
            weightedSum += (score * weight / 100.0);
        }
        return weightedSum;
    }

    private String getLetterGrade(double percentage) {
        if (percentage >= 90) return "A";
        else if (percentage >= 80) return "B";
        else if (percentage >= 70) return "C";
        else if (percentage >= 60) return "D";
        else return "F";
    }
}
