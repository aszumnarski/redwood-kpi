package com.redwood.scheduler.custom.kpi.kpi3.model;

public record ClearingResult(
        int proposed,
        int itemsCleared,
        int errors)
{
}
