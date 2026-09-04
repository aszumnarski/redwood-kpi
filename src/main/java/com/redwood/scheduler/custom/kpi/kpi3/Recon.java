package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;

import java.io.PrintWriter;

public class Recon
{
    private Job j;

    public Recon(Job j)
            throws Exception
    {
        this.j = j;
    }

    public void collectActionItems(SchedulerSession session,PrintWriter p, Job job)
            throws Exception
    {

    }
}