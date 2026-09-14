package com.redwood.scheduler.custom.kpi.kpi3.config;

import java.util.Set;

public record CollectorConfig(
        String outputCategory,
        Set<String> baseParents,
        Set<String> dtParents,
        boolean collectRtx,
        boolean restrictDtParents,
        boolean unrestrictedBaseWorking)
{
}
