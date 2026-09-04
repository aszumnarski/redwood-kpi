package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;

import java.io.PrintWriter;

public class ConditionalClearingSuggestion
{
    private int totalOpenItems = 0;
    private int autoClear = 0;
    private int totalCollected = 0;
    private int selectedForClear = 0;
    private Job j;

    public ConditionalClearingSuggestion(Job j)
    {
        this.j = j;
    }
    private void addAi(AccountItem ai,PrintWriter p)
    {
        totalOpenItems += ai.getTotalOpenItems();
        autoClear += ai.getAutoClear();
        totalCollected += ai.getTotalCollected();
    }

    private void addAi(AccountItemGL ai,PrintWriter p)
    {
        totalOpenItems += ai.getTotalOpenItems();
        autoClear += ai.getAutoClear();
        totalCollected += ai.getTotalCollected();
    }

    public void collectActionItems(SchedulerSession session,PrintWriter p,Job job)
            throws Exception
    {
        scan(session,j,p,job);
    }
    private void scan(SchedulerSession session,Job parent,PrintWriter p,Job job)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {
            if(relevantJobA(parent,child))
            {
                p.println("Account Item - scan found A " + child.getJobId());
                AccountItem ai = new AccountItem(child,j.getRunStart());
                ai.collectChildren(session,p,job);
                addAi(ai,p);
                ai = null;
            }
            if(relevantJobB(parent,child))
            {
                p.println("Account Item - scan found B " + child.getJobId());
                AccountItemGL aigl = new AccountItemGL(child,j.getRunStart());
                aigl.collectChildren(session,p,job);
                addAi(aigl,p);
                aigl = null;
            }
            scan(session,child,p,job);
        }
    }
    boolean relevantJobA(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        return (p + c).equals("CUS_FCA_TD_BSC_AccountItemLoop_CONDITIONAL_AUTOCLEAR_REVIEWCUS_TD_BSC_CONDITIONAL_AUTOCLEAR_REVIEW");
    }
    boolean relevantJobB(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        return (p + c).equals("FCA_SAP_Generic_LoopCUS_TD_BSC_CONDITIONAL_AUTOCLEAR_REVIEW_WEA");
    }
}