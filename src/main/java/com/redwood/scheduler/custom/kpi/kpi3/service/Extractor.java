package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.date.DateTimeZone;
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
        DateTimeZone dtz1 = DateTimeZone.now();
        String query = getQuery(session, extractorJob);
        out.println("Extractor.execute query: " + query);
        Iterator<Job> it = session.executeObjectQuery(Job.TYPE,query);
        DateTimeZone dtz2 = DateTimeZone.now();
        long elapsedSec = (dtz2.getUTCMilliSecs() - dtz1.getUTCMilliSecs()) / 1000;
        out.println("Extractor.execute query executed in " + elapsedSec + " sec");
        dtz1 = DateTimeZone.now();
        collector.collectAll(it,out);
        dtz2 = DateTimeZone.now();
        elapsedSec = (dtz2.getUTCMilliSecs() - dtz1.getUTCMilliSecs()) / 1000;
        out.println("Extractor.execute jobs collected in " + elapsedSec + " sec");
        out.println(collector.getStatistics());
        dtz1 = DateTimeZone.now();
        processor.processNoSum(collector.getWorkItems(),session, extractorJob,out);
        dtz2 = DateTimeZone.now();
        elapsedSec = (dtz2.getUTCMilliSecs() - dtz1.getUTCMilliSecs()) / 1000;
        out.println("Extractor.execute jobs processed in " + elapsedSec + " sec");
    }
}