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
    public void processNoSum(Iterable<WorkItem> items, SchedulerSession session, Job extractorJob, PrintWriter p)
            throws Exception
    {
        int counter = 1;
        int total = (items instanceof Collection) ? ((Collection<?>) items).size() : -1;

        for (WorkItem item : items)
        {
            DateTimeZone dtz1 = new DateTimeZone();
            processWorkItem(item, session, extractorJob, p);
            DateTimeZone dtz2 = new DateTimeZone();
            long elapsedSec = (dtz2.getUTCMilliSecs() - dtz1.getUTCMilliSecs()) / 1000;
            p.println("Loop " + counter + (total > 0 ? " of " + total : "") + ": " + item.getType() + " - " + item.getBukrs() + " - accountGroup=" + item.getAccountGroup() + " (" + item.getJob().getJobId() + ") processed in " + elapsedSec + " seconds.");
            counter ++;
        }
    }

    private void processWorkItem(WorkItem item, SchedulerSession session, Job extractorJob,PrintWriter p)
            throws Exception
    {
        String definitionName = item.getJob().getJobDefinition().getMasterJobDefinition().getName();
        p.println("Processor.processWorkItem: definitionName" + definitionName);
        JobDefinitionRegistry definition = JobDefinitionRegistry.fromName(definitionName);

        if (definition == null)
        {
            p.println("Processor.processWorkItem: definition not found!");
            writeMissingChain(definitionName, session, extractorJob);
            return;
        }
        p.println("Processor.processWorkItem: definition found, processing using handler " + definition.handler.name() + " ...");
        definition.handler.handle(item.getJob(), session, p, extractorJob);

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