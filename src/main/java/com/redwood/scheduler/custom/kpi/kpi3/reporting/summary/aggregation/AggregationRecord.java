package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.aggregation;

import com.redwood.scheduler.custom.kpi.kpi3.model.KPICounts;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingKey;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;
import java.util.HashSet;
import java.util.Set;

public class AggregationRecord
{
    private final ReportingKey key;

    Set<String> openDocs = new HashSet<>();
    Set<String> proposedDocs = new HashSet<>();
    Set<String> clearedDocs = new HashSet<>();
    Set<String> errorDocs = new HashSet<>();

    KPICounts fallbackCounts = new KPICounts();

    public AggregationRecord(ReportingKey reportingKey){
        this.key = reportingKey;
    }

    public void addKPICounts(KPICounts counts) {
        fallbackCounts.add(counts);
    }

    public void addOpen(Set<String> uniqueLines) {
        openDocs.addAll(uniqueLines);
    }

    public void addProposed(Set<String> uniqueLines) {
        proposedDocs.addAll(uniqueLines);
    }

    public void addCleared(Set<String> uniqueLines) {
        clearedDocs.addAll(uniqueLines);
    }

    public void addError(Set<String> uniqueLines) {
        errorDocs.addAll(uniqueLines);
    }

    public ReportingRecord toReportingRecord()
    {
        ReportingRecord record = new ReportingRecord(key);

        record.setTotal(openDocs.isEmpty() ? fallbackCounts.getOpen() : openDocs.size());
        record.setProposed(proposedDocs.isEmpty() ? fallbackCounts.getProposed() : proposedDocs.size());
        record.setCleared(clearedDocs.isEmpty() ? fallbackCounts.getCleared() : clearedDocs.size());
        record.setError(errorDocs.isEmpty() ? fallbackCounts.getErrors() : errorDocs.size());

        return record;
    }

    public void setKPICounts(KPICounts counts) {
        this.fallbackCounts = counts;
    }
}