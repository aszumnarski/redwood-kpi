package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;

import java.io.PrintWriter;

class AccountItemAutoLedger implements AccountItemSource
{

    private Long clearId = -1L;
    private Long prepId;
    private int totalOpenItems = 0;
    private int itemsCleared = 0;
    private int ruleSet1 = 0;
    private int autoClear = 0;
    private int errors = 0;
    private int suggestedClear = 0;
    private int totalCollected = 0;

    private final AccountItemContext context;


    public AccountItemAutoLedger(Job job,DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(job, parentDate);
    }

    private void addLedger(Ledger l,PrintWriter p)
    {
        ruleSet1 += l.getRuleSet1();
        autoClear += l.getAutoClear();
        suggestedClear += l.getSuggestedClear();
        totalCollected += l.getTotalCollected();
        totalOpenItems += l.getTotalOpenItems();
        itemsCleared += l.getItemsCleared();
        errors += l.getErrors();
    }

    public int getRuleSet1()
    {
        return ruleSet1;
    }
    public int getAutoClear()
    {
        return autoClear;
    }
    public int getSuggestedClear()
    {
        return suggestedClear;
    }
    public int getItemsCleared()
    {
        return itemsCleared;
    }
    public int getTotalOpenItems()
    {
        return totalOpenItems;
    }
    public int getTotalCollected()
    {
        return totalCollected;
    }
    public int getErrors()
    {
        return errors;
    }
    public void collectChildren(SchedulerSession session,PrintWriter p,Job job)
            throws Exception
    {
        scan(session, context.getJob(), p,job);
    }
    private void scan(SchedulerSession session, Job parent, PrintWriter p,Job job)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {
            if(relevantJob(parent,child))
            {
                Ledger l = new Ledger(child,context.getParentDate());
                l.collectChildren(session,p,job);
                addLedger(l,p);
            }
            else if(relevantJob2(parent,child))
            {
                Ledger l = new Ledger(child,context.getParentDate());
                l.collectChildren(session,p,job);
                addLedger(l,p);
            }
            scan(session,child,p,job);
        }
    }
    boolean relevantJob(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        return ((p + c).equals("FCA_SAP_Generic_LoopCUS_SPD_BSC_AUTOCLEAR_SHERPAX_LDGRP"));
    }

    boolean relevantJob2(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        return ((p + c).equals("FCA_SAP_Generic_LoopCUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_LDGRP_US"));
    }
    public String getStatus()
    {
        return context.getStatus();
    }
    public String getName()
    {
        return context.getName();
    }
    public Long getClearId()
    {
        return clearId;
    }
    public Long getPrepId()
    {
        return prepId;
    }
    public String getAccountGroup()
    {
        return context.getAccountGroup();
    }
    public String getCompanyCode()
    {
        return context.getCompanyCode();
    }

    public AccountItemContext getContext() {
        return context;
    }
}