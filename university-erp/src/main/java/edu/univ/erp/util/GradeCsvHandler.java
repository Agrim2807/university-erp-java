package edu.univ.erp.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.StudentGradeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;


public class GradeCsvHandler {
    private static final Logger logger = LoggerFactory.getLogger(GradeCsvHandler.class);

    
    public static boolean exportGradesToCsv(List<StudentGradeEntry> students,
                                           List<GradeComponent> components,
                                           File outputFile) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {

            
            List<String> header = new ArrayList<>();
            
            header.add("Roll Number");
            header.add("Student Name");

            for (GradeComponent component : components) {
                header.add(component.getComponentName() + " (/" + component.getMaxScore() + ")");
            }

            header.add("Final Grade");

            writer.writeNext(header.toArray(new String[0]));

            
            for (StudentGradeEntry student : students) {
                List<String> row = new ArrayList<>();
                row.add(student.getRollNo());
                row.add(student.getStudentName());

                
                for (GradeComponent component : components) {
                    Double score = student.getScores().get(component.getComponentId());
                    row.add(score != null ? String.valueOf(score) : "");
                }

                
                row.add(student.getFinalGrade() != null ? student.getFinalGrade() : "");

                writer.writeNext(row.toArray(new String[0]));
            }

            logger.info("Grades exported to CSV: {}", outputFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            logger.error("Error exporting grades to CSV", e);
            return false;
        }
    }

    
    public static CsvImportResult importGradesFromCsv(File inputFile,
                                                      List<GradeComponent> components,
                                                      Map<String, Integer> rollToEnrollmentMap) {
        Map<Integer, Map<Integer, Double>> importedScores = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try (CSVReader reader = new CSVReader(new FileReader(inputFile))) {

            String[] header = reader.readNext();
            if (header == null || header.length < 2) {
                return new CsvImportResult(false, 0, Collections.singletonList("Invalid CSV format: missing headers"));
            }

            
            
            Map<Integer, Integer> columnToComponentId = new HashMap<>();

            for (int i = 2; i < header.length; i++) { 
                String columnName = header[i];
                if ("Final Grade".equalsIgnoreCase(columnName.trim())) continue; 

                
                String componentName = columnName.replaceAll("\\s*\\(/.*\\)$", "").trim();

                
                boolean found = false;
                for (GradeComponent comp : components) {
                    if (comp.getComponentName().equalsIgnoreCase(componentName)) {
                        columnToComponentId.put(i, comp.getComponentId());
                        found = true;
                        break;
                    }
                }
                
            }

            
            String[] line;
            int lineNumber = 1;

            while ((line = reader.readNext()) != null) {
                lineNumber++;

                if (line.length < 2) {
                    
                    continue;
                }

                
                String rollNo = line[0].trim();
                
                
                Integer enrollmentId = rollToEnrollmentMap.get(rollNo);
                
                if (enrollmentId == null) {
                    
                    errors.add("Line " + lineNumber + ": Student '" + rollNo + "' is not enrolled in this section.");
                    continue;
                }

                Map<Integer, Double> studentScores = new HashMap<>();

                
                for (Map.Entry<Integer, Integer> entry : columnToComponentId.entrySet()) {
                    int columnIndex = entry.getKey();
                    int componentId = entry.getValue();

                    if (columnIndex < line.length && !line[columnIndex].trim().isEmpty()) {
                        try {
                            double score = Double.parseDouble(line[columnIndex].trim());

                            
                            GradeComponent component = components.stream()
                                .filter(c -> c.getComponentId() == componentId)
                                .findFirst()
                                .orElse(null);

                            if (component != null) {
                                if (score < 0) {
                                    errors.add("Line " + lineNumber + ": Negative score not allowed");
                                    continue;
                                }
                                if (score > component.getMaxScore()) {
                                    errors.add("Line " + lineNumber + ": Score " + score +
                                             " exceeds max score " + component.getMaxScore() +
                                             " for " + component.getComponentName());
                                    continue;
                                }
                                studentScores.put(componentId, score);
                            }

                        } catch (NumberFormatException e) {
                            errors.add("Line " + lineNumber + ": Invalid score value: " + line[columnIndex]);
                        }
                    }
                }

                if (!studentScores.isEmpty()) {
                    importedScores.put(enrollmentId, studentScores);
                    successCount++;
                }
            }

            logger.info("CSV import completed: {} students processed, {} errors", successCount, errors.size());
            return new CsvImportResult(true, successCount, errors, importedScores);

        } catch (Exception e) {
            logger.error("Error importing grades from CSV", e);
            return new CsvImportResult(false, 0, Collections.singletonList("Error reading file: " + e.getMessage()));
        }
    }

    public static boolean exportRosterToCsv(List<StudentGradeEntry> students, File outputFile) {
         
         
         return false; 
    }

    
    public static class CsvImportResult {
        private final boolean success;
        private final int recordsProcessed;
        private final List<String> errors;
        private final Map<Integer, Map<Integer, Double>> importedScores;

        public CsvImportResult(boolean success, int recordsProcessed, List<String> errors) {
            this(success, recordsProcessed, errors, new HashMap<>());
        }

        public CsvImportResult(boolean success, int recordsProcessed, List<String> errors,
                             Map<Integer, Map<Integer, Double>> importedScores) {
            this.success = success;
            this.recordsProcessed = recordsProcessed;
            this.errors = errors;
            this.importedScores = importedScores;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getRecordsProcessed() {
            return recordsProcessed;
        }

        public List<String> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public Map<Integer, Map<Integer, Double>> getImportedScores() {
            return importedScores;
        }

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("Records processed: ").append(recordsProcessed).append("\n");
            if (hasErrors()) {
                sb.append("Errors/Warnings: ").append(errors.size()).append("\n");
                sb.append("Details:\n");
                for (int i = 0; i < Math.min(10, errors.size()); i++) {
                    sb.append("  - ").append(errors.get(i)).append("\n");
                }
                if (errors.size() > 10) {
                    sb.append("  ... and ").append(errors.size() - 10).append(" more");
                }
            } else {
                sb.append("No errors!");
            }
            return sb.toString();
        }
    }
}