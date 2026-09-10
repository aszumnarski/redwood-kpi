package com.redwood.scheduler.custom.kpi.kpi3.model;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;

public final class WorkItem
{
    private final String period;
    private final JobType type;
    private final String bukrs;
    private final String accountGroup;
    private final Job job;

    public WorkItem(String period,JobType type,String bukrs,String accountGroup,Job job)
    {
        this.period = period;
        this.type = type;
        this.bukrs = bukrs;
        this.accountGroup = accountGroup;
        this.job = job;
    }

    public String getPeriod() {return period;}
    public JobType getType() {return type;}
    public String getBukrs() {return bukrs;}
    public String getAccountGroup() {return accountGroup;}
    public Job getJob() {return job;}

    @Override
    public String toString()
    {
        return "WorkItem{" +
                "period='" + period + '\'' +
                ", outputCategory='" + type.toString().toLowerCase() + '\'' +
                ", bukrs='" + bukrs + '\'' +
                ", accountGroup='" + accountGroup + '\'' +
                ", jobId=" + job.getJobId() +
                '}';
    }
}
