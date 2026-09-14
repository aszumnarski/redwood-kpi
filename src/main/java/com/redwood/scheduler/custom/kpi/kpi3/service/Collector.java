package com.redwood.scheduler.custom.kpi.kpi3.service;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobDefinitionRegistry;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.model.WorkItem;

import java.util.*;
import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.date.PeriodUtil.getPeriod;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getAccountGroups;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

public class Collector
{
    private final List<WorkItem> workItems = new ArrayList<>();
    private int jobsCollected;
    private int unknownJobs;
    private final Map<JobType,Integer> typeCounts = new EnumMap<>(JobType.class);

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
        jobsCollected++;
        JobDefinitionRegistry def = JobDefinitionRegistry.fromName(j.getJobDefinition().getMasterJobDefinition().getName());
        JobType type = def != null ? def.type : JobType.UNKNOWN;
        if (type == JobType.UNKNOWN) { unknownJobs++; }
        typeCounts.merge(type, 1, Integer::sum);

        String period = getPeriod(j.getRunStart());
        String bukrs = getParameter(j,"BUKRS");
        String accountGroup = getAccountGroups(j);
        workItems.add(new WorkItem(period,type,bukrs,accountGroup,j));
    }

    public Iterable<WorkItem> getWorkItems() {return workItems;}

    public String getStatistics() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("========================================\n");
        sb.append("COLLECTOR STATISTICS\n");
        sb.append("========================================\n");

        sb.append("Jobs collected : ").append(jobsCollected).append("\n");
        sb.append("Unknown jobs   : ").append(unknownJobs).append("\n");
        sb.append("Work items     : ").append(workItems.size()).append("\n");

        sb.append("\nJob types:\n");

        typeCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e ->
                        sb.append(String.format("  %-20s %8d%n",
                                e.getKey(),
                                e.getValue())));

        sb.append("========================================\n");

        return sb.toString();
    }

}