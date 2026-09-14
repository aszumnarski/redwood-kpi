package com.redwood.scheduler.custom.kpi.kpi3.orchestration;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.Stopwatch;
import com.redwood.scheduler.custom.kpi.kpi3.service.Collector;
import com.redwood.scheduler.custom.kpi.kpi3.service.WorkItemProcessor;

import java.io.PrintWriter;
import java.util.Iterator;

import com.redwood.scheduler.custom.kpi.kpi3.query.JobQueryBuilder;

public class ExtractionRunner
{

    private final WorkItemProcessor workItemProcessor;
    private final Collector collector;
    private final SchedulerSession session;
    private final Job extractorJob;
    private final PrintWriter out;

    public ExtractionRunner(WorkItemProcessor workItemProcessor, Collector collector, SchedulerSession session, Job extractorJob, PrintWriter out)
    {
        this.workItemProcessor = workItemProcessor;
        this.collector = collector;
        this.session = session;
        this.extractorJob = extractorJob;
        this.out = out;
    }

    public void execute()
            throws Exception
    {
        Stopwatch stopwatch = new Stopwatch();
        String query = JobQueryBuilder.getQuery(session, extractorJob);
        out.println("Extractor.execute query: " + query);
        Iterator<Job> it = session.executeObjectQuery(Job.TYPE,query);
        stopwatch.log(out,"Extractor.execute query executed");
        stopwatch.reset();
        collector.collectAll(it,out);
        stopwatch.log(out,"Extractor.execute jobs collected");
        out.println(collector.getStatistics());
        stopwatch.reset();
        workItemProcessor.processNoSum(collector.getWorkItems(),session, extractorJob,out);
        stopwatch.log(out,"Extractor.execute jobs processed");

        ResultFileWriter writer = new ResultFileWriter();

        writer.writeItemsToNamedFile(
                session,
                extractorJob,
                "file_statistics.csv",
                ResultFileWriter.getStatistics().toCsvRows()
        );

    }
}