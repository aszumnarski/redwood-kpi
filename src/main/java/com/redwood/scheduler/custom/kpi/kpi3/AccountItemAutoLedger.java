package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.rtx.RTXReader;
import com.redwood.scheduler.api.rtx.RTXRow;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.PrintWriter;

class AccountItemAutoLedger
{
    private DateTimeZone runStartDate;
    private DateTimeZone runEndDate;
    private DateTimeZone parentDate;
    private String status;
    private String name;
    private Long id;
    private String accountGroup;
    private String companyCode;
    private Long clearId = -1l;
    private Long prepId;
    private int totalOpenItems = 0;
    private Set<String> totalOpenItemsSet;
    private int itemsCleared = 0;
    private Set<String> itemsClearedSet;
    private int ruleSet1 = 0;
    private int autoClear = 0;
    private int errors = 0;
    private Set<String> errorsSet;
    private int suggestedClear = 0;
    private int totalCollected = 0;
    private Set<String> totalCollectedSet;
    private Job j;

    public AccountItemAutoLedger(Job j, DateTimeZone parentDate)
            throws Exception
    {
        this.parentDate = parentDate;
        this.j = j;
        id = j.getJobId();
        runStartDate = j.getRunStart();
        runEndDate = j.getRunEnd();
        status = j.getStatus().getTranslationEN();
        name = j.getJobDefinition().getName();
        accountGroup = Util.getAccountGroups(j);
        companyCode = Util.getParameter(j,"BUKRS");
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
        scan(session, j,p,job);
    }
    private void scan(SchedulerSession session, Job parent, PrintWriter p,Job job)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {
            if(relevantJob(parent,child))
            {
                Ledger l = new Ledger(child,parentDate);
                l.collectChildren(session,p,job);
                addLedger(l,p);
                l = null;
            }
            else if(relevantJob2(parent,child))
            {
                Ledger l = new Ledger(child,parentDate);
                l.collectChildren(session,p,job);
                addLedger(l,p);
                l = null;
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
    public Set<String> getTotalOpenItemsSet()
    {
        return totalOpenItemsSet;
    }
    public Set<String> getTotalCollectedSet()
    {
        return totalCollectedSet;
    }
    public Set<String> getItemsClearedSet()
    {
        return itemsClearedSet;
    }
    public Set<String> getErrorsSet()
    {
        return errorsSet;
    }
    public DateTimeZone getRunStartDate()
    {
        return runStartDate;
    }
    public DateTimeZone getRunEndDate()
    {
        return runEndDate;
    }
    public String getStatus()
    {
        return status;
    }
    public String getName()
    {
        return name;
    }
    public Long getId()
    {
        return id;
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
        return accountGroup;
    }
    public String getCompanyCode()
    {
        return companyCode;
    }

}