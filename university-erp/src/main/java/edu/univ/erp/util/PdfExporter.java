package edu.univ.erp.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.StudentGradeEntry;
import edu.univ.erp.domain.TranscriptEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;


public class PdfExporter {
    private static final Logger logger = LoggerFactory.getLogger(PdfExporter.class);

    
    private static final Color HEADER_BG = new Color(41, 128, 185);
    private static final Color TABLE_HEADER_BG = new Color(52, 152, 219);
    private static final Color ALT_ROW_BG = new Color(236, 240, 241);

    
    public static boolean exportTranscript(List<TranscriptEntry> entries, String username,
                                          String rollNo, String program, File outputFile) {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();

            addHeader(document, "UNIVERSITY ERP SYSTEM");
            addTitle(document, "Academic Transcript");
            addStudentInfo(document, username, rollNo, program);
            document.add(new Paragraph("\n"));
            addTranscriptTable(document, entries);
            addFooter(document);

            logger.info("PDF transcript generated successfully: {}", outputFile.getAbsolutePath());
            return true;

        } catch (Exception e) {
            logger.error("Error generating PDF transcript", e);
            return false;
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    
    public static boolean exportSectionGradeReport(List<StudentGradeEntry> students,
                                                   List<GradeComponent> components,
                                                   String courseCode,
                                                   String sectionCode,
                                                   String instructorName,
                                                   File outputFile) {
        
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            document.open();

            addHeader(document, "UNIVERSITY ERP SYSTEM");
            addTitle(document, "Class Grade Report");

            
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new int[]{1, 1});
            
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            
            metaTable.addCell(createNoBorderCell("Course: " + courseCode + " - Section " + sectionCode, labelFont));
            metaTable.addCell(createNoBorderCell("Instructor: " + instructorName, labelFont));
            
            
            metaTable.addCell(createNoBorderCell("Date: " + LocalDate.now().toString(), valueFont));
            metaTable.addCell(createNoBorderCell("Total Students: " + students.size(), valueFont));
            
            metaTable.setSpacingAfter(15);
            document.add(metaTable);

            
            
            int numComponents = components.size();
            int totalCols = 4 + numComponents; 
            
            PdfPTable table = new PdfPTable(totalCols);
            table.setWidthPercentage(100);
            
            
            float[] widths = new float[totalCols];
            widths[0] = 0.5f; 
            widths[1] = 1.5f; 
            widths[2] = 3.0f; 
            for (int i = 0; i < numComponents; i++) {
                widths[3 + i] = 1.5f; 
            }
            widths[totalCols - 1] = 1.2f; 
            table.setWidths(widths);

            
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            
            addHeaderCell(table, "#", headerFont);
            addHeaderCell(table, "Roll No", headerFont);
            addHeaderCell(table, "Student Name", headerFont);
            
            for (GradeComponent comp : components) {
                String compHeader = String.format("%s\n(%.0f%%)", comp.getComponentName(), comp.getWeight());
                addHeaderCell(table, compHeader, headerFont);
            }
            
            addHeaderCell(table, "Final\nGrade", headerFont);

            
            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            int rowCount = 0;

            for (StudentGradeEntry student : students) {
                Color bgColor = (rowCount % 2 == 0) ? Color.WHITE : ALT_ROW_BG;
                
                addTableCell(table, String.valueOf(rowCount + 1), rowFont, bgColor, Element.ALIGN_CENTER);
                addTableCell(table, student.getRollNo(), rowFont, bgColor, Element.ALIGN_CENTER);
                addTableCell(table, student.getStudentName(), rowFont, bgColor, Element.ALIGN_LEFT);
                
                
                for (GradeComponent comp : components) {
                    Double score = student.getScores().get(comp.getComponentId());
                    String scoreText = (score != null) ? String.format("%.1f", score) : "-";
                    addTableCell(table, scoreText, rowFont, bgColor, Element.ALIGN_CENTER);
                }
                
                
                String finalGrade = student.getFinalGrade() != null ? student.getFinalGrade() : "-";
                addTableCell(table, finalGrade, rowFont, bgColor, Element.ALIGN_CENTER);
                
                rowCount++;
            }

            document.add(table);
            addFooter(document);

            logger.info("PDF section grade report generated: {}", outputFile.getAbsolutePath());
            return true;

        } catch (Exception e) {
            logger.error("Error generating PDF grade report", e);
            return false;
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    

    private static PdfPCell createNoBorderCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        return cell;
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(TABLE_HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private static void addHeader(Document document, String universityName) throws DocumentException {
        Paragraph header = new Paragraph();
        header.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK));
        header.setAlignment(Element.ALIGN_CENTER);
        header.add(universityName);
        header.setSpacingAfter(10);
        document.add(header);
    }

    private static void addTitle(Document document, String title) throws DocumentException {
        Paragraph titlePara = new Paragraph();
        titlePara.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, HEADER_BG));
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.add(title);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);
    }

    private static void addStudentInfo(Document document, String username,
                                      String rollNo, String program) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new int[]{1, 2});

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        addInfoRow(infoTable, "Student Name:", username, labelFont, valueFont);
        addInfoRow(infoTable, "Roll Number:", rollNo, labelFont, valueFont);
        addInfoRow(infoTable, "Program:", program, labelFont, valueFont);
        addInfoRow(infoTable, "Generated:", LocalDate.now().toString(), labelFont, valueFont);

        document.add(infoTable);
    }

    private static void addInfoRow(PdfPTable table, String label, String value,
                                   Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    
    private static void addTranscriptTable(Document document, List<TranscriptEntry> entries)
            throws DocumentException {
        
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        
        table.setWidths(new int[]{2, 5, 1, 2, 1, 2});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        
        String[] headers = {"Course", "Title", "Credits", "Semester", "Year", "Final Grade"};

        for (String header : headers) {
            addHeaderCell(table, header, headerFont);
        }

        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        int rowCount = 0;
        int totalCredits = 0;

        for (TranscriptEntry entry : entries) {
            Color bgColor = (rowCount % 2 == 0) ? Color.WHITE : ALT_ROW_BG;

            addTableCell(table, entry.getCourseCode(), rowFont, bgColor, Element.ALIGN_LEFT);
            addTableCell(table, entry.getCourseTitle(), rowFont, bgColor, Element.ALIGN_LEFT);
            addTableCell(table, String.valueOf(entry.getCredits()), rowFont, bgColor, Element.ALIGN_CENTER);
            addTableCell(table, entry.getSemester(), rowFont, bgColor, Element.ALIGN_CENTER);
            addTableCell(table, String.valueOf(entry.getYear()), rowFont, bgColor, Element.ALIGN_CENTER);
            
            
            
            String grade = entry.getFinalGrade() != null ? entry.getFinalGrade() : "IP";
            addTableCell(table, grade, rowFont, bgColor, Element.ALIGN_CENTER);

            if (entry.getFinalGrade() != null && !entry.getFinalGrade().equals("F") && !entry.getFinalGrade().equals("IP")) {
                totalCredits += entry.getCredits();
            }
            rowCount++;
        }

        
        PdfPCell summaryCell = new PdfPCell(new Phrase("Total Credits Earned: " + totalCredits,
                                           FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        summaryCell.setColspan(6);
        summaryCell.setBackgroundColor(new Color(241, 196, 15));
        summaryCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryCell.setPadding(8);
        table.addCell(summaryCell);

        document.add(table);
    }
    

    
    public static boolean exportClassRoster(List<String[]> students, String courseCode,
                                           String sectionCode, String instructorName, File outputFile) {
         return false; 
    }

    private static void addTableCell(PdfPTable table, String text, Font font,
                                     Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private static void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.setFont(FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        footer.add("Generated on " + LocalDate.now() + " by University ERP System");
        footer.add("\nThis is a computer-generated document. No signature is required.");
        document.add(footer);
    }
}