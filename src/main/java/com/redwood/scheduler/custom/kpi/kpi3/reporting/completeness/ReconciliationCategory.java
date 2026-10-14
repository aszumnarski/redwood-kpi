package com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness;

public class ReconciliationCategory {

    private final ReconKey reconKey;
    private final String sourceMasterTable;

    public ReconciliationCategory(
            ReconKey reconKey,
            String sourceMasterTable) {

        this.reconKey = reconKey;
        this.sourceMasterTable = sourceMasterTable;

    }

    public ReconKey getReconKey() {
        return reconKey;
    }

    public String getSourceMasterTable() {
        return sourceMasterTable;
    }
}
