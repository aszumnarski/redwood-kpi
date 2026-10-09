package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.io.PrintWriter;

public interface AccountItemSource
{
    void collectChildren(SchedulerSession session, PrintWriter p, Job job, Logger log) throws Exception;
    AccountItemContext getContext();
    CollectorStats getStats();

}

