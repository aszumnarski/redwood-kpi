package com.redwood.scheduler.custom.kpi.kpi3;

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
    private final Job job;
    private final PrintWriter out;

    public Extractor(Processor processor, Collector collector, SchedulerSession session, Job job, PrintWriter out)
    {
        this.processor = processor;
        this.collector = collector;
        this.session = session;
        this.job = job;
        this.out = out;
    }

    public void execute()
            throws Exception
    {
        String query = getQuery(session,job);
        out.println(query);
        Iterator<Job> it = session.executeObjectQuery(query, null);
        collector.collectAll(it,out);
        processor.processNoSum(collector.getWorkItems(),session,job,out);
    }
}