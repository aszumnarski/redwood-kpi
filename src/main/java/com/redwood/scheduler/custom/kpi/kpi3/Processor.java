package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.custom.kpi.kpi3.Util.JobDefinitionRegistry;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.date.DateTimeZone;

import java.io.PrintWriter;
import java.util.Collection;
import java.io.FileOutputStream;

public class Processor
{
    public void processNoSum(Iterable<WorkItem> items, SchedulerSession session, Job parentJob, PrintWriter p)
            throws Exception
    {
        int counter = 1;
        int total = (items instanceof Collection) ? ((Collection<?>) items).size() : -1;

        for (WorkItem item : items)
        {
            DateTimeZone dtz1 = new DateTimeZone();
            processWorkItem(item, session, parentJob, p);
            DateTimeZone dtz2 = new DateTimeZone();
            long elapsedSec = (dtz2.getUTCMilliSecs() - dtz1.getUTCMilliSecs()) / 1000;
            p.println("Loop " + counter + (total > 0 ? " of " + total : "") + ": " + item.getType() + " - " + item.getBukrs() + " - accountGroup=" + item.getAccountGroup() + " (" + item.getJob().getJobId() + ") processed in " + elapsedSec + " seconds.");
            counter ++;
        }
    }

    private void processWorkItem(WorkItem item, SchedulerSession session, Job parentJob,PrintWriter p)
            throws Exception
    {
        JobDefinitionRegistry definition = JobDefinitionRegistry.fromName(item.getJob().getJobDefinition().getName());

        if (definition == null)
        {
            writeMissingChain(definition.name, session, parentJob);
            return;
        }

        definition.handler.handle(item.getJob(), session, p, parentJob);

    }


    private void writeMissingChain(String definition,SchedulerSession session,Job parentJob)
            throws Exception
    {
        String i = "Unknown definition found: " + definition;

        boolean append = true;
        String fileName = "missing_chains.csv";
        JobFile jf = parentJob.getJobFileByName(fileName);
        if (jf == null)
        {
            jf = Util.createJobFile(session,parentJob,fileName);
            append = false;
        }
        try (FileOutputStream out = new FileOutputStream(jf.getFileName(), append))
        {
            Util.write(out, i);
        }
    }
}