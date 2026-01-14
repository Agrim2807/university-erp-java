package edu.univ.erp.util;

import com.opencsv.CSVWriter; 
import edu.univ.erp.domain.TranscriptEntry;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvExporter {

    public static void exportTranscript(List<TranscriptEntry> entries, File file) throws IOException {

        try (CSVWriter writer = new CSVWriter(new FileWriter(file),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {

            
            String[] header = {"Course Code", "Course Title", "Credits", "Semester", "Year", "Component Grades", "Final Grade"};
            writer.writeNext(header);

            
            List<String[]> data = new ArrayList<>();
            for (TranscriptEntry entry : entries) {
                data.add(new String[]{
                        entry.getCourseCode(),
                        entry.getCourseTitle(),
                        String.valueOf(entry.getCredits()),
                        entry.getSemester(),
                        String.valueOf(entry.getYear()),
                        entry.getComponentGradesAsString(), 
                        entry.getFinalGrade() 
                });
            }

            
            writer.writeAll(data);
        }
    }

    
    
}
