package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.custom.kpi.kpi3.file.ClearingResultProcessor;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.service.RTXService;

import java.util.List;
import java.util.Map;
import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

class Ledger
{
    private Long clearId = -1L;
    private int totalOpenItems = 0;
    private int itemsCleared = 0;
    private int ruleSet1 = 0;
    private int autoClear = 0;
    private int errors = 0;
    private int suggestedClear = 0;
    private int totalCollected = 0;

    private final AccountItemContext context;
    private final ResultFileWriter fileWriter;
    private final ClearingResultProcessor clearingProcessor;


    public Ledger(Job job, DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(job, parentDate);
        this.fileWriter = new ResultFileWriter(context,"auto");
        this.clearingProcessor = new ClearingResultProcessor(fileWriter);
    }

    private void addDt(DataTransformer dt)
    {
        if (ruleSet1 == 0) ruleSet1 = dt.getRuleSet1();
        autoClear += dt.getAutoClear();
        suggestedClear += dt.getSuggestedClear();
        totalCollected = autoClear + suggestedClear;
    }

    //List<String> clearingParents = List.of("CUS_SPD_AutoClearing_ErrorReport","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_Phase4","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_SOFOM,CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_US","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_Phase5");
    //List<String> collectParents = List.of("CUS_SPD_BSC_AUTOCLEAR_RULES_WEA_new","CUS_SPD_BSC_AUTOCLEAR_RULES_Sherpax_Global_new","CUS_SPD_BSC_AUTOCLEAR_RULES_Sherpax_Focus4_new","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_Phase4","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_SOFOM","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_US","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_Phase5");
    public final List<String> baseParents = List.of("CUS_TD_BSC_AUTOCLEAR_WEA","CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_Original","CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH","CUS_TD_BSC_AUTOCLEAR_SHERPAX_new","CUS_TD_BSC_AUTOCLEAR_Cashmatching_E1P_US","CUS_SPD_BSC_AUTOCLEAR_SHERPAX_LDGRP","CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_LDGRP_US");
    public final List<String> dtParents = List.of("CUS_SPD_BSC_AUTOCLEAR_RULES_WEA_new","CUS_SPD_BSC_AUTOCLEAR_RULES_Sherpax_Global_new","CUS_SPD_BSC_AUTOCLEAR_RULES_Sherpax_Focus4_new","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_Phase5");

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
    public void collectChildren(SchedulerSession session,PrintWriter p, Job job)
            throws Exception
    {
        scan(session, context.getJob(), p,job);
    }
    private void scan(SchedulerSession session,Job parent,PrintWriter pw,Job job)
            throws Exception
    {

        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        for(Job child: parent.getChildJobs())
        {
            String c = child.getJobDefinition().getMasterJobDefinition().getName();
            if(baseParents.contains(p)&&(c).contains("BaseWorking"))
            {
                DataTransformer tot = new DataTransformer(session,child,true,pw,job, context.getCompanyCode(), context.getAccountGroup(), "auto",context.getParentDate());
                totalOpenItems = tot.getRuleSet1();
                //totalOpenItemsSet = tot.getTotalCollectedSet();
            }
            else if(dtParents.contains(p) && c.startsWith("CUS_DT"))
            {
                DataTransformer dt = new DataTransformer(session,child,false,pw,job,context.getCompanyCode(), context.getAccountGroup(),"auto",context.getParentDate());
                addDt(dt);
            }
            else if(c.equals("CUS_TRN_COLLECT_RTX"))
            {
                DataTransformer dt = new DataTransformer(session,child,false,pw,job,context.getCompanyCode(), context.getAccountGroup(),"auto",context.getParentDate());
                totalCollected = dt.getTotalCollected();
            }
            else if(c.equals("FCA_SAP_Tran_FB05_Clearing"))
            {
                ClearingResult result = clearingProcessor.process(session, child, job, totalCollected);
                clearId = result.clearId();
                itemsCleared += result.itemsCleared();
                errors += result.errors();
            }
            scan(session,child,pw,job);
        }

    }

    public String getStatus()
    {
        return context.getStatus();
    }
    public String getName()
    {
        return context.getName();
    }
    public String getAccountGroup()
    {
        return context.getAccountGroup();
    }
    public String getCompanyCode()
    {
        return context.getCompanyCode();
    }
}