package edu.univ.erp.ui;

import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.StudentGradeEntry;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class GradebookTableModel extends DefaultTableModel {

    private List<GradeComponent> components;
    private List<StudentGradeEntry> students;
    private List<String> columnNames;

    public GradebookTableModel() {
        this.students = new ArrayList<>();
        this.components = new ArrayList<>();
        this.columnNames = new ArrayList<>();
        updateColumnNames();
    }

    
    public void setData(List<GradeComponent> components, List<StudentGradeEntry> students) {
        this.components = (components != null) ? new ArrayList<>(components) : new ArrayList<>();
        this.students = (students != null) ? new ArrayList<>(students) : new ArrayList<>();
        updateColumnNames();
        fireTableStructureChanged();
    }

    private void updateColumnNames() {
        columnNames.clear();
        columnNames.add("Roll No");
        columnNames.add("Student Name");
        if (this.components != null) {
            for (GradeComponent comp : this.components) {
                columnNames.add(comp.getComponentName() + " (" + String.format("%.1f", comp.getMaxScore()) + ")");
            }
        }
        columnNames.add("Final Grade");
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int column) {
        if (column >= 0 && column < columnNames.size()) {
            return columnNames.get(column);
        }
        return super.getColumnName(column);
    }

    @Override
    public int getRowCount() {
        return (students != null) ? students.size() : 0;
    }

    @Override
    public Object getValueAt(int row, int col) {
        
        if (students == null || row < 0 || row >= students.size()) {
            return null;
        }
        StudentGradeEntry student = students.get(row);
        if (col < 0 || col >= columnNames.size()) {
             return null;
        }

        if (col == 0) return student.getRollNo();
        if (col == 1) return student.getStudentName();
        if (col == columnNames.size() - 1) return student.getFinalGrade();

        int componentIndex = col - 2;
        if (components != null && componentIndex >= 0 && componentIndex < components.size()) {
            GradeComponent comp = components.get(componentIndex);
            return student.getScores().get(comp.getComponentId());
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {
        if (students == null || row < 0 || row >= students.size() ||
            col < 2 || col >= columnNames.size() - 1) {
            return;
        }

        StudentGradeEntry student = students.get(row);
        int componentIndex = col - 2;

        if (components != null && componentIndex >= 0 && componentIndex < components.size()) {
            GradeComponent comp = components.get(componentIndex);
            try {
                if (aValue == null || aValue.toString().trim().isEmpty()) {
                     student.getScores().remove(comp.getComponentId());
                     fireTableCellUpdated(row, col);

                } else {
                    double score = Double.parseDouble(aValue.toString());
                    
                    if (score < 0) {
                        javax.swing.SwingUtilities.invokeLater(() ->
                            MainFrame.getInstance().showError("Score cannot be negative."));
                        return;
                    }
                    if (score > comp.getMaxScore()) {
                        javax.swing.SwingUtilities.invokeLater(() ->
                            MainFrame.getInstance().showError("Score cannot exceed maximum (" + String.format("%.1f", comp.getMaxScore()) + ") for " + comp.getComponentName() + "."));
                        return;
                    }
                    student.addScore(comp.getComponentId(), score);
                    fireTableCellUpdated(row, col);
                }
            } catch (NumberFormatException e) {
                javax.swing.SwingUtilities.invokeLater(() ->
                    MainFrame.getInstance().showError("Please enter a valid number."));
            }
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col >= 2 && col < (columnNames.size() - 1);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex >= 2 && columnIndex < (columnNames.size() - 1)) {
            return Double.class;
        }
        return String.class;
    }
}