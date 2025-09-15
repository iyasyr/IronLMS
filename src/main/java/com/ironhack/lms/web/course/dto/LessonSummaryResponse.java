package com.ironhack.lms.web.course.dto;

public record LessonSummaryResponse(
        Long id,
        String title,
        String contentUrl,
        int orderIndex
) {}
