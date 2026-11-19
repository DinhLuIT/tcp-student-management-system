package com.ptit.studentmgmt.common.dto;

import java.io.Serializable;

public class StudentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String studentId;
    private String fullName;
    private double mathScore;
    private double literatureScore;
    private double englishScore;

    public StudentDTO() {
    }

    public StudentDTO(String studentId, String fullName, double mathScore, double literatureScore, double englishScore) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.mathScore = mathScore;
        this.literatureScore = literatureScore;
        this.englishScore = englishScore;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public double getMathScore() {
        return mathScore;
    }

    public void setMathScore(double mathScore) {
        this.mathScore = mathScore;
    }

    public double getLiteratureScore() {
        return literatureScore;
    }

    public void setLiteratureScore(double literatureScore) {
        this.literatureScore = literatureScore;
    }

    public double getEnglishScore() {
        return englishScore;
    }

    public void setEnglishScore(double englishScore) {
        this.englishScore = englishScore;
    }

    @Override
    public String toString() {
        return "StudentDTO{" +
                "studentId='" + studentId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", mathScore=" + mathScore +
                ", literatureScore=" + literatureScore +
                ", englishScore=" + englishScore +
                '}';
    }
}
