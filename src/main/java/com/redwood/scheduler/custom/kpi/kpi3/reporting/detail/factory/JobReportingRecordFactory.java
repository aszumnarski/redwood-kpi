package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.factory;

import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.date.PeriodUtil;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileAllocation;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemContext;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;

public class JobReportingRecordFactory
{
    public JobReportingRecord create(
            AccountItemContext context,
            CollectorStats stats,
            FileAllocation allocation,
            JobType jobType)
    {
        JobReportingRecord record = new JobReportingRecord();

        record.setJobId(context.getId());
        record.setJobName(context.getName());
        record.setRunDate(context.getRunStartDate());
        record.setScheduleDate(context.getParentDate());
        record.setStatus(context.getStatus());
        record.setCompanyCode(context.getCompanyCode());
        record.setAccountItem(context.getAccountGroup());
        record.setClearingType(jobType);
        record.setTotalOpenItems(stats.getTotalOpenItems());
        record.setProposed(stats.getTotalCollected());
        record.setCleared(stats.getItemsCleared());
        record.setErrors(stats.getErrors());
        record.setSourceFiles(allocation);

        record.setPeriod(PeriodUtil.getPeriod(context.getParentDate()));

        return record;
    }
}

