package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.Job;

import java.io.PrintWriter;
import java.util.Iterator;

import static com.redwood.scheduler.custom.kpi.kpi3.query.JobQueryBuilder.getQuery;

public class Extractor
{

    private final Processor processor;
    private final Collector collector;
    private final SchedulerSession session;
    private final Job extractorJob;
    private final PrintWriter out;

    public Extractor(Processor processor, Collector collector, SchedulerSession session, Job extractorJob, PrintWriter out)
    {
        this.processor = processor;
        this.collector = collector;
        this.session = session;
        this.extractorJob = extractorJob;
        this.out = out;
    }

    public void execute()
            throws Exception
    {
        String query = getQuery(session, extractorJob);
        out.println(query);
        Iterator<Job> it = session.executeObjectQuery(query, null);
        collector.collectAll(it,out);
        processor.processNoSum(collector.getWorkItems(),session, extractorJob,out);
    }
}