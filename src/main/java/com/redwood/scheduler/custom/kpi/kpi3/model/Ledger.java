package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;

import java.io.PrintWriter;

class Ledger {
    private final LeafCollector collector;

    public Ledger(Job job, DateTimeZone parentDate)
            throws Exception {
        collector = new LeafCollector(job, parentDate, CollectorConfigs.LEDGER);
    }

    public void collectChildren(SchedulerSession session, PrintWriter p, Job job)
            throws Exception {
        collector.collectChildren(session, p, job);
    }

    public CollectorStats getStats(){
        return collector.getStats();
    }

    public AccountItemContext getContext()
    {
        return collector.getContext();
    }
}