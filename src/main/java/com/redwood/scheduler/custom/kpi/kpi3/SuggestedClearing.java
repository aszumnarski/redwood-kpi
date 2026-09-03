package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

public class SuggestedClearing
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

    public SuggestedClearing(Job j)
            throws Exception
    {
        this.j = j;
        id = j.getJobId();
        runStartDate = j.getRunStart();
        runEndDate = j.getRunEnd();
        status = j.getStatus().getTranslationEN();
        name = j.getJobDefinition().getName();
        accountGroups = Util.getAccountGroups(j);
        companyCode = Util.getParameter(j,"BUKRS");
    }

    public void collectActionItems(SchedulerSession session,PrintWriter p, Job job)
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
                p.println("Suggested DIV - scan found 1 " + child.getJobId());
                SuggestedClearingDIV div = new SuggestedClearingDIV(child,runStartDate);
                div.collectChildren(session,p,job);
                addDiv(div,p);
                div = null;
            }
            else if(relevantJob2(parent,child,p))
            {
                p.println("Suggested DIV - scan found 2 " + child.getJobId());
                SuggestedClearingDIV div = new SuggestedClearingDIV(child,runStartDate);
                div.collectChildren(session,p,job);
                addDiv(div,p);
                div = null;
            }
            scan(session,child,p,job);
        }
    }

    boolean relevantJob1(Job parent,Job child,PrintWriter pw)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //pw.println(p + "_______" + c);
        return (p + c).equals("CUS_FCA_TD_BSC_AccountItemLoop_SuggestedClearing_SHERPAXCUS_FCA_TD_BSC_DIVLoop_SuggestedClearing_OneSided_MASTER_SHERPAX");
    }

    boolean relevantJob2(Job parent,Job child,PrintWriter pw)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //pw.println(p + "_______" + c);
        return (p + c).equals("CUS_FCA_TD_BSC_AccountItemLoop_SuggestedClearing_OneSided_WEACUS_FCA_TD_BSC_DIVLoop_SuggestedClearing_OneSided_MASTER");
    }

    private void addDiv(SuggestedClearingDIV div,PrintWriter p)
    {
        totalOpenItems += div.getTotalOpenItems();
        autoClear += div.getAutoClear();
        suggestedClear += div.getSuggestedClear();
        itemsCleared += div.getItemsCleared();
        selectedForClear += div.getTotalCollected();
        errors += div.getErrors();
    }
}