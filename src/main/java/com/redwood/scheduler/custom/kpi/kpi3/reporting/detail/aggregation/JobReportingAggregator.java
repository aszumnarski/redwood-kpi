package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.aggregation;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;

import java.util.ArrayList;
import java.util.List;

public class JobReportingAggregator
{
    private final List<JobReportingRecord> records =
            new ArrayList<>();

    public void add(JobReportingRecord record)
    {
        records.add(record);
    }

    public List<JobReportingRecord> getRecords()
    {
        return records;
    }
}