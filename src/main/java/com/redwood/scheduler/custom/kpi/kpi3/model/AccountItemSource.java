package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;

import java.io.PrintWriter;

public interface AccountItemSource
{
    void collectChildren(SchedulerSession session, PrintWriter p, Job job) throws Exception;
    AccountItemContext getContext();
    CollectorStats getStats();

}

