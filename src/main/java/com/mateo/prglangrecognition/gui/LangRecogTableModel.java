package com.mateo.prglangrecognition.gui;

import java.util.Map;
import javax.swing.table.AbstractTableModel;

public class LangRecogTableModel extends AbstractTableModel {

    @Override
    public int getRowCount() {
        if (langRecogResult == null)
            return 0;
        else
            return langRecogResult.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int rowCounter = 0;
        for (Map.Entry<String, Double> item : langRecogResult.entrySet()) {
            if (rowCounter == rowIndex)
                return (columnIndex == 0 ? item.getKey() : item.getValue());
            else
                rowCounter++;
        }

        throw new RuntimeException("Invalid implementation");
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void loadResult(Map<String, Double> result) {
        langRecogResult = result;
        fireTableDataChanged();
    }

    private static final String columnNames[] = {"Language", "Propability %"};
    Map<String, Double> langRecogResult = null;
}