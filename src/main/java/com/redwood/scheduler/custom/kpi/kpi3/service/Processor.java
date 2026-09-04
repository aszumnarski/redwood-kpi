package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.custom.kpi.kpi3.config.JobDefinitionRegistry;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.custom.kpi.kpi3.model.WorkItem;

import java.io.PrintWriter;
import java.util.Collection;
import java.io.FileOutputStream;

import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;

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
        String definitionName = item.getJob().getJobDefinition().getMasterJobDefinition().getName();
        JobDefinitionRegistry definition = JobDefinitionRegistry.fromName(definitionName);

        if (definition == null)
        {
            writeMissingChain(definitionName, session, parentJob);
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
            jf = createJobFile(session,parentJob,fileName);
            append = false;
        }
        try (FileOutputStream out = new FileOutputStream(jf.getFileName(), append))
        {
            write(out, i);
        }
    }
}