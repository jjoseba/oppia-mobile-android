package org.digitalcampus.mobile.quiz.model;

import java.util.Comparator;

public class GradeBoundary {
    private int grade;
    private String message;

    public GradeBoundary(int grade, String message) {
        this.grade = grade;
        this.message = message;
    }

    public int getGrade() {
        return grade;
    }

    public String getMessage() {
        return message;
    }

    public static final Comparator<GradeBoundary> sortByGradeDescending = (gradeBoundary1, gradeBoundary2) -> gradeBoundary2.grade - gradeBoundary1.grade;
}
