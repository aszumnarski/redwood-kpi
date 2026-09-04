package com.redwood.scheduler.custom.kpi.kpi3.service;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobDefinitionRegistry;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.model.WorkItem;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.util.Iterator;

import static com.redwood.scheduler.custom.kpi.kpi3.date.PeriodUtil.getPeriod;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getAccountGroups;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

public class Collector
{
    private final List<WorkItem> workItems = new ArrayList<>();

    public void collectAll(Iterator<Job> it, PrintWriter p)
            throws Exception
    {
        if (it == null) return;
        while (it.hasNext())
        {
            collectJob(it.next(),p);
        }

    }
    private void collectJob(Job j, PrintWriter p)
            throws Exception
    {
        JobDefinitionRegistry def = JobDefinitionRegistry.fromName(j.getJobDefinition().getName());
        JobType type = def != null ? def.type : JobType.UNKNOWN;

        String period = getPeriod(j.getRunStart());
        String bukrs = getParameter(j,"BUKRS");
        String accountGroup = getAccountGroups(j);
        workItems.add(new WorkItem(period,type,bukrs,accountGroup,j));
    }

    public Iterable<WorkItem> getWorkItems() {return workItems;}

    public void print(PrintWriter p)
    {
        for (WorkItem wi : workItems)
        {
            p.println(wi.getPeriod() + ";" + wi.getType().toString().toLowerCase() + ";" + wi.getBukrs() + ";" + wi.getAccountGroup() + ";" + wi.getJob().getJobId());
        }
    }

}