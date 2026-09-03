package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getAccountGroups;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

class AccountItemGL
{
    private DateTimeZone runStartDate;
    private DateTimeZone runEndDate;
    private DateTimeZone parentDate;
    private String status;
    private String name;
    private Long id;
    private String accountGroup;
    private String companyCode;
    private int ruleSet1 = 0;
    private int autoClear = 0;
    private int suggestedClear = 0;

    private int totalCollected = 0;
    private int totalOpenItems = 0;

    private Job j;

    public AccountItemGL(Job j,DateTimeZone parentDate)
            throws Exception
    {
        this.parentDate = parentDate;
        this.j = j;
        id = j.getJobId();
        runStartDate = j.getRunStart();
        runEndDate = j.getRunEnd();
        status = j.getStatus().getTranslationEN();
        name = j.getJobDefinition().getName();
        accountGroup = getAccountGroups(j);
        companyCode = getParameter(j,"BUKRS");
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
    public String getAccountGroup()
    {
        return accountGroup;
    }
    public String getCompanyCode()
    {
        return companyCode;
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
        scan(session,j,p,job);
    }
    private void scan(SchedulerSession session,Job parent, PrintWriter p, Job job)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {
            if(relevantJob(parent,child))
            {
                p.println("Account Item GL - scan found " + child.getJobId());
                GL gl = new GL(child,parentDate);
                gl.collectChildren(session,p,job);
                addGl(gl);
                gl = null;
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
}