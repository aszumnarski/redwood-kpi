package com.redwood.scheduler.custom.kpi.kpi3;
import com.redwood.scheduler.custom.kpi.kpi3.Util.JobDefinitionRegistry;
import com.redwood.scheduler.custom.kpi.kpi3.Util.JobType;
import com.redwood.scheduler.api.model.Job;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.util.Iterator;

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

        String period = Util.getPeriod(j.getRunStart());
        String bukrs = Util.getParameter(j,"BUKRS");
        String accountGroup = Util.getAccountGroups(j);
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