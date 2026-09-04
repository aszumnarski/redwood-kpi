package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;

import java.io.PrintWriter;


class AccountItemGL
{
    private final AccountItemContext context;
    private int ruleSet1 = 0;
    private int autoClear = 0;
    private int suggestedClear = 0;

    private int totalCollected = 0;
    private int totalOpenItems = 0;

    public AccountItemGL(Job job,DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(job, parentDate);
    }
    private void addGl(GL gl)
    {
        ruleSet1 += gl.getRuleSet1();
        autoClear += gl.getAutoClear();
        suggestedClear += gl.getSuggestedClear();
        totalCollected += gl.getTotalCollected();
        totalOpenItems += gl.getTotalOpenItems();
    }
    public DateTimeZone getRunStartDate()
    {
        return context.getRunStartDate();
    }
    public DateTimeZone getRunEndDate()
    {
        return context.getRunEndDate();
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
    public int getTotalOpenItems()
    {
        return totalOpenItems;
    }
    public int getTotalCollected()
    {
        return totalCollected;
    }
    public void collectChildren(SchedulerSession session,PrintWriter p, Job job)
            throws Exception
    {
        scan(session,context.getJob(),p,job);
    }
    private void scan(SchedulerSession session,Job parent, PrintWriter p, Job job)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {
            if(relevantJob(parent,child))
            {
                p.println("Account Item GL - scan found " + child.getJobId());
                GL gl = new GL(child,context.getParentDate());
                gl.collectChildren(session,p,job);
                addGl(gl);
            }
            scan(session,child,p,job);
        }
    }
    boolean relevantJob(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getName();
        String c = child.getJobDefinition().getName();
        return ((p + c).equals("FCA_SAP_Generic_LoopCUS_TD_BSC_CONDITIONAL_AUTOCLEAR_REVIEW_GL"));
    }

    public AccountItemContext getContext() {
        return context;
    }
}