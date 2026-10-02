package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.aggregation;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingKey;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ReportingAggregator
{
    private final Map<ReportingKey, ReportingRecord> records =
            new HashMap<>();

    public ReportingRecord get(ReportingKey key)
    {
        return records.get(key);
    }

    public void add(ReportingRecord record)
    {
        records.putIfAbsent(record.getReportingKey(), record);
    }

    public Collection<ReportingRecord> getRecords()
    {
        return records.values();
    }

    public boolean contains(ReportingKey key)
    {
        return records.containsKey(key);
    }

    public void mergeConditionalSummary(
            ReportingRecord record)
    {
        ReportingRecord existing = records.get(record.getReportingKey());

        if(existing == null)
        {
            records.put(record.getReportingKey(), record);
            return;
        }

        existing.getCounts().setOpen(record.getCounts().getOpen());
        existing.getCounts().setProposed(record.getCounts().getProposed());
    }

    public void mergeConditionalResults(
            ReportingRecord record)
    {
        ReportingRecord existing = records.get(record.getReportingKey());

        if(existing == null)
        {
            records.put(record.getReportingKey(), record);
            return;
        }

        existing.getCounts().setCleared(record.getCounts().getCleared());
        existing.getCounts().setErrors(record.getCounts().getErrors());

    }

}
