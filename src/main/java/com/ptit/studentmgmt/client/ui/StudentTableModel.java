package com.ptit.studentmgmt.client.ui;

import com.ptit.studentmgmt.common.dto.StudentResultDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class StudentTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private final String[] columnNames = {
            "STT", "Mã Sinh Viên", "Họ và Tên", "Điểm Toán", "Điểm Văn", "Điểm Tiếng Anh", "Điểm Trung Bình", "Xếp Loại"
    };
    private final Class<?>[] columnClasses = {
            Integer.class, String.class, String.class, Double.class, Double.class, Double.class, Double.class, String.class
    };

    private List<StudentResultDTO> studentList = new ArrayList<>();

    public void setStudents(List<StudentResultDTO> list) {
        this.studentList = list != null ? new ArrayList<>(list) : new ArrayList<>();
        fireTableDataChanged();
    }

    public void clear() {
        this.studentList.clear();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return studentList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= studentList.size()) return null;
        StudentResultDTO s = studentList.get(rowIndex);
        switch (columnIndex) {
            case 0: return rowIndex + 1;
            case 1: return s.getStudentId();
            case 2: return s.getFullName();
            case 3: return s.getMathScore();
            case 4: return s.getLiteratureScore();
            case 5: return s.getEnglishScore();
            case 6: return s.getAverageScore();
            case 7: return s.getAcademicRank();
            default: return null;
        }
    }

    public StudentResultDTO getStudentAt(int row) {
        if (row >= 0 && row < studentList.size()) {
            return studentList.get(row);
        }
        return null;
    }
}
