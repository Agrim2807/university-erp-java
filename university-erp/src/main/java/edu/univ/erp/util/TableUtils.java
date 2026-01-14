package edu.univ.erp.util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.awt.Color;


public class TableUtils {

    
    public static void enableSorting(JTable table) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
    }

    
    public static void disableSortingForColumns(JTable table, int... columnIndices) {
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) table.getRowSorter();
        if (sorter != null) {
            for (int columnIndex : columnIndices) {
                sorter.setSortable(columnIndex, false);
            }
        }
    }

    
    public static void applyAlternatingRowColors(JTable table) {
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    
    public static void applyAlternatingRowColors(JTable table, Color evenRowColor, Color oddRowColor) {
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer(evenRowColor, oddRowColor));
    }

    
    public static void centerAlignColumns(JTable table, int... columnIndices) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int columnIndex : columnIndices) {
            if (columnIndex < table.getColumnCount()) {
                table.getColumnModel().getColumn(columnIndex).setCellRenderer(centerRenderer);
            }
        }
    }

    
    public static void rightAlignColumns(JTable table, int... columnIndices) {
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int columnIndex : columnIndices) {
            if (columnIndex < table.getColumnCount()) {
                table.getColumnModel().getColumn(columnIndex).setCellRenderer(rightRenderer);
            }
        }
    }

    
    public static void autoResizeColumns(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    
    public static void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    
    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        private final Color evenRowColor;
        private final Color oddRowColor;

        public AlternatingRowRenderer() {
            this(Color.WHITE, new Color(240, 240, 240));
        }

        public AlternatingRowRenderer(Color evenRowColor, Color oddRowColor) {
            this.evenRowColor = evenRowColor;
            this.oddRowColor = oddRowColor;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected,
                                                             hasFocus, row, column);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? evenRowColor : oddRowColor);
            }

            return c;
        }
    }

    
    public static void highlightRows(JTable table, RowHighlighter highlighter) {
        table.setDefaultRenderer(Object.class, new ConditionalRowRenderer(highlighter));
    }

    
    public interface RowHighlighter {
        Color getColorForRow(JTable table, int row);
    }

    
    private static class ConditionalRowRenderer extends DefaultTableCellRenderer {
        private final RowHighlighter highlighter;

        public ConditionalRowRenderer(RowHighlighter highlighter) {
            this.highlighter = highlighter;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected,
                                                             hasFocus, row, column);

            if (!isSelected) {
                Color color = highlighter.getColorForRow(table, row);
                if (color != null) {
                    c.setBackground(color);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                }
            }

            return c;
        }
    }

    
    public static void makeTableNonEditable(JTable table) {
        table.setDefaultEditor(Object.class, null);
    }

    
    public static void refreshTable(JTable table) {
        ((javax.swing.table.DefaultTableModel) table.getModel()).fireTableDataChanged();
    }
}
