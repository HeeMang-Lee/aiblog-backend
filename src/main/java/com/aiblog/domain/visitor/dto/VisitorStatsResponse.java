package com.aiblog.domain.visitor.dto;

public record VisitorStatsResponse(
    long totalCount,
    long todayCount
) {

}
