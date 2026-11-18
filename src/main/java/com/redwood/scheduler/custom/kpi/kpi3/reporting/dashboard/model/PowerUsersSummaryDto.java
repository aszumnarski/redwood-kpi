package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model;

import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;

public class PowerUsersSummaryDto {
    private long autoCount;
    private long conditionalCount;
    private long suggestedCount;
    private long reconCount;
    private long unknownCount;

    public long getAutoCount() {
        return autoCount;
    }

    public void setAutoCount(long autoCount) {
        this.autoCount = autoCount;
    }

    public long getConditionalCount() {
        return conditionalCount;
    }

    public void setConditionalCount(long conditionalCount) {
        this.conditionalCount = conditionalCount;
    }

    public long getSuggestedCount() {
        return suggestedCount;
    }

    public void setSuggestedCount(long suggestedCount) {
        this.suggestedCount = suggestedCount;
    }

    public long getReconCount() {
        return reconCount;
    }

    public void setReconCount(long reconCount) {
        this.reconCount = reconCount;
    }

    public long getUnknownCount() {
        return unknownCount;
    }

    public void setUnknownCount(long unknownCount) {
        this.unknownCount = unknownCount;
    }

    public void setCount(JobType type, Long value){
        switch (type){
            case AUTO -> autoCount = value;
            case RECON -> reconCount = value;
            case UNKNOWN -> unknownCount = value;
            case SUGGESTED -> suggestedCount = value;
            case CONDITIONAL -> conditionalCount = value;
        }
    }
}
