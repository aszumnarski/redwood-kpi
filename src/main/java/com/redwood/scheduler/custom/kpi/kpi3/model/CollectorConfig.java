package com.redwood.scheduler.custom.kpi.kpi3.model;

import java.util.Set;

public record CollectorConfig(
        Set<String> baseParents,
        Set<String> dtParents,
        boolean collectRtx)
{
}
