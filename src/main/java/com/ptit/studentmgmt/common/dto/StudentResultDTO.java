package com.ptit.studentmgmt.common.dto;

import java.io.Serializable;

public class StudentResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String studentId;
    private String fullName;
    private double mathScore;
    private double literatureScore;
    private double englishScore;
    private double averageScore;
    private String academicRank;

    public StudentResultDTO() {
    }

    public StudentResultDTO(String studentId, String fullName, double mathScore, double literatureScore, double englishScore, double averageScore, String academicRank) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.mathScore = mathScore;
        this.literatureScore = literatureScore;
        this.englishScore = englishScore;
        this.averageScore = averageScore;
        this.academicRank = academicRank;
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

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public String getAcademicRank() {
        return academicRank;
    }

    public void setAcademicRank(String academicRank) {
        this.academicRank = academicRank;
    }

    @Override
    public String toString() {
        return "StudentResultDTO{" +
                "studentId='" + studentId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", mathScore=" + mathScore +
                ", literatureScore=" + literatureScore +
                ", englishScore=" + englishScore +
                ", averageScore=" + averageScore +
                ", academicRank='" + academicRank + '\'' +
                '}';
    }
}
