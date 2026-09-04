package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;

import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getAccountGroups;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

public class SuggestedClearingDIV
{
    private DateTimeZone runStartDate;
    private DateTimeZone runEndDate;
    private DateTimeZone parentDate;
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
    private int ruleSet1 = 0;
    private int totalCollected = 0;
    private Job j;

    public SuggestedClearingDIV(Job j, DateTimeZone parentDate)
            throws Exception
    {
        this.j = j;
        this.parentDate = parentDate;
        id = j.getJobId();
        runStartDate = j.getRunStart();
        runEndDate = j.getRunEnd();
        status = j.getStatus().getTranslationEN();
        name = j.getJobDefinition().getName();
        accountGroups = getAccountGroups(j);
        companyCode = getParameter(j,"BUKRS");
    }

    public void collectChildren(SchedulerSession session,PrintWriter p, Job job)
            throws Exception
    {
        scan(session,j,p,job);
    }

    private void scan(SchedulerSession session, Job parent,PrintWriter p, Job job)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {
            if(relevantJob1(parent,child,p))
            {
                p.println("Account Item Suggested - scan found 1 " + child.getJobId());
                AccountItemSuggested ai = new AccountItemSuggested(child,runStartDate);
                ai.collectChildren(session,p,job);
                addAi(ai,p);
                ai = null;
            }
            else if(relevantJob2(parent,child,p))
            {
                p.println("Account Item Suggested - scan found 2 " + child.getJobId());
                AccountItemSuggested ai = new AccountItemSuggested(child,runStartDate);
                ai.collectChildren(session,p,job);
                addAi(ai,p);
                ai = null;
            }
            else if(relevantJob3(parent,child,p))
            {
                p.println("Account Item Suggested - scan found 3 " + child.getJobId());
                AccountItemSuggested ai = new AccountItemSuggested(child,runStartDate);
                ai.collectChildren(session,p,job);
                addAi(ai,p);
                ai = null;
            }
            else if(relevantJob4(parent,child,p))
            {
                p.println("Account Item Suggested - scan found 4 " + child.getJobId());
                AccountItemSuggested ai = new AccountItemSuggested(child,runStartDate);
                ai.collectChildren(session,p,job);
                addAi(ai,p);
                ai = null;
            }
            scan(session,child,p,job);
        }
    }

    boolean relevantJob1(Job parent,Job child,PrintWriter pw)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //pw.println(p + "_______" + c);
        return (p + c).equals("FCA_SAP_Generic_LoopCUS_TD_BSC_SuggestedClearing_OneSided_MASTER_SHERPAX");
    }

    boolean relevantJob2(Job parent,Job child,PrintWriter pw)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //pw.println(p + "_______" + c);
        return (p + c).equals("PRE_FCA_SAP_Generic_LoopCUS_TD_BSC_SuggestedClearing_OneSided_MASTER_SHERPAX");
    }

    boolean relevantJob3(Job parent,Job child,PrintWriter pw)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //pw.println(p + "_______" + c);
        return (p + c).equals("FCA_SAP_Generic_LoopCUS_TD_BSC_SuggestedClearing_OneSided_MASTER");
    }

    boolean relevantJob4(Job parent,Job child,PrintWriter pw)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //pw.println(p + "_______" + c);
        return (p + c).equals("PRE_FCA_SAP_Generic_LoopCUS_TD_BSC_SuggestedClearing_OneSided_MASTER");
    }

    private void addAi(AccountItemSuggested ai,PrintWriter p)
    {
        totalOpenItems += ai.getTotalOpenItems();
        autoClear += ai.getAutoClear();
        suggestedClear += ai.getSuggestedClear();
        itemsCleared += ai.getItemsCleared();
        selectedForClear += ai.getTotalCollected();
        errors += ai.getErrors();
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

}