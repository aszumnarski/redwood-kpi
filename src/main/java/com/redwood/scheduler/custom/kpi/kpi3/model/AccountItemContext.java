package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getAccountGroups;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

public class AccountItemContext
{
    private final Job job;
    private final DateTimeZone parentDate;

    private final Long id;
    private final DateTimeZone runStartDate;
    private final DateTimeZone runEndDate;
    private final String status;
    private final String name;
    private final String accountGroup;
    private final String companyCode;

    public AccountItemContext(Job job, DateTimeZone parentDate)
            throws Exception
    {
        this.job = job;
        this.parentDate = parentDate;

        this.id = job.getJobId();
        this.runStartDate = job.getRunStart();
        this.runEndDate = job.getRunEnd();
        this.status = job.getStatus().getTranslationEN();
        this.name = job.getJobDefinition().getName();
        this.accountGroup = getAccountGroups(job);
        this.companyCode = getParameter(job, "BUKRS");
    }

    public Job getJob()
    {
        return job;
    }

    public DateTimeZone getParentDate()
    {
        return parentDate;
    }

    public Long getId()
    {
        return id;
    }

    public DateTimeZone getRunStartDate()
    {
        return runStartDate;
    }

    public DateTimeZone getRunEndDate()
    {
        return runEndDate;
    }

    public String getStatus()
    {
        return status;
    }

    public String getName()
    {
        return name;
    }

    public String getAccountGroup()
    {
        return accountGroup;
    }

    public String getCompanyCode()
    {
        return companyCode;
    }
}