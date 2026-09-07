package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;

import java.io.PrintWriter;

class AccountItemAutoOld implements AccountItemSource
{
    private final LeafCollector collector;

    public AccountItemAutoOld(Job job,DateTimeZone parentDate)
            throws Exception
    {
        collector = new LeafCollector(job, parentDate, CollectorConfigs.AUTO_OLD);
    }

    public void collectChildren(SchedulerSession session, PrintWriter p, Job job)
            throws Exception
    {
        collector.collectChildren(session, p,job);
    }

    @Override
    public AccountItemContext getContext() {
        return collector.getContext();
    }

    @Override
    public CollectorStats getStats() {
        return collector.getStats();
    }
}