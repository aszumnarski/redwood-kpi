package com.redwood.scheduler.custom.kpi.kpi3.model;

public record ClearingResult(
        Long clearId,
        int itemsCleared,
        int errors)
{
}
