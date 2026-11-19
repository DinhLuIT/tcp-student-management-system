package com.ptit.studentmgmt.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BatchStudentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;
    private int totalRecords;
    private List<StudentDTO> students;

    public BatchStudentDTO() {
        this.students = new ArrayList<>();
    }

    public BatchStudentDTO(String fileName, List<StudentDTO> students) {
        this.fileName = fileName;
        this.students = students != null ? students : new ArrayList<>();
        this.totalRecords = this.students.size();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public List<StudentDTO> getStudents() {
        return students;
    }

    public void setStudents(List<StudentDTO> students) {
        this.students = students;
        this.totalRecords = students != null ? students.size() : 0;
    }

    @Override
    public String toString() {
        return "BatchStudentDTO{" +
                "fileName='" + fileName + '\'' +
                ", totalRecords=" + totalRecords +
                ", studentsCount=" + (students != null ? students.size() : 0) +
                '}';
    }
}
