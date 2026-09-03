package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

public class Recon
{
    private DateTimeZone runStartDate;
    private DateTimeZone runEndDate;
    private String status;
    private String name;
    private Long id;
    private String accountGroups;
    private String companyCode;
    private int totalOpenItems = 0;
    private int selectedForClear = 0;
    private int itemsCleared = 0;
    private int errors = 0;
    private int autoClear = 0;
    private int suggestedClear = 0;
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