package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.service.RTXService;

import java.util.List;
import java.util.Set;
import java.util.Map;

import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

class AccountItemAutoOld implements AccountItemSource
{
    private Long clearId = -1L;
    private long prepId;
    private int totalOpenItems = 0;
    private int itemsCleared = 0;
    private int ruleSet1 = 0;
    private int autoClear = 0;
    private int errors = 0;
    private int suggestedClear = 0;
    private int totalCollected = 0;

    private final AccountItemContext context;
    private final ResultFileWriter fileWriter;

    public AccountItemAutoOld(Job job,DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(job, parentDate);
        this.fileWriter = new ResultFileWriter(context,"auto");
    }

    private void addDt(DataTransformer dt)
    {
        if (ruleSet1 == 0) ruleSet1 = dt.getRuleSet1();
        autoClear += dt.getAutoClear();
        suggestedClear += dt.getSuggestedClear();
        totalCollected += autoClear + suggestedClear;
    }
    public String getStatus()
    {
        return context.getStatus();
    }
    public String getName()
    {
        return context.getName();
    }
    public Long getId()
    {
        return context.getId();
    }
    public Long getClearId()
    {
        return clearId;
    }
    public Long getPrepId()
    {
        return prepId;
    }
    public int getTotalOpenItems()
    {
        return totalOpenItems;
    }
    public int getTotalCollected()
    {
        return totalCollected;
    }
    public String getAccountGroup()
    {
        return context.getAccountGroup();
    }
    public String getCompanyCode()
    {
        return context.getCompanyCode();
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
    public int getErrors()
    {
        return errors;
    }
    public void collectChildren(SchedulerSession session, PrintWriter p, Job job)
            throws Exception
    {
        scan(session, context.getJob(), p,job);
    }
    private void scan(SchedulerSession session,Job parent,PrintWriter pw, Job job)
            throws Exception
    {

        Set<String> totalOpenItemsParents = Set.of("CUS_TD_BSC_AUTOCLEAR_WEA","CUS_TD_BSC_AUTOCLEAR_CB_IN");
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        for(Job child: parent.getChildJobs())
        {
            String c = child.getJobDefinition().getMasterJobDefinition().getName();
            //jcsOut.println(p+c);
            if(totalOpenItemsParents.contains(p)&&(c).contains("BaseWorking"))
            {
                DataTransformer tot = new DataTransformer(session,child,true,pw,job, context.getCompanyCode(), context.getAccountGroup(), "auto",context.getParentDate());
                totalOpenItems = tot.getRuleSet1();
                //totalOpenItemsSet = tot.getTotalCollectedSet();
            }
            else if(c.startsWith("CUS_DT"))
            {
                DataTransformer dt = new DataTransformer(session,child,false,pw,job, context.getCompanyCode(), context.getAccountGroup(), "auto",context.getParentDate());
                addDt(dt);
            }
            else if(c.equals("FCA_SAP_Tran_FB05_Clearing"))
            {
                String okLines = getParameter(child,"OUT_DATA_OK_RTX");
                String errorLines = getParameter(child,"OUT_DATA_ERROR_RTX");
                Map<String,List<String>> matchings = RTXService.getMatchingRtx(child,"BELNR","BUZEI","StartNewTransaction");
                if(errorLines == null)
                {
                    if(!matchings.isEmpty()) itemsCleared += fileWriter.writeItemsToFile(session, job,"cleared", matchings);
                }
                else if(okLines == null)
                {
                    if(!matchings.isEmpty()) errors += fileWriter.writeItemsToFile(session,job,"errors", matchings);
                }
                else
                {
                    WriteResult result = fileWriter.writeErrorsAndClearedSeparately(session,child,job,matchings);
                    itemsCleared += result.clearedCount();
                    errors += result.errorCount();
                }
            }
            scan(session,child,pw,job);
        }
    }

    public AccountItemContext getContext() {
        return context;
    }
}