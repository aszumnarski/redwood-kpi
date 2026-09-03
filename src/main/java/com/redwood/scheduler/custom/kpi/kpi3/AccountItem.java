package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getAccountGroups;

class AccountItem
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

    public AccountItem(Job j,DateTimeZone parentDate)
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
    }
    public AccountItem(AccountItemGL aigl)
    {
        runStartDate = aigl.getRunStartDate();
        runEndDate = aigl.getRunEndDate();
        status = aigl.getStatus();
        name = aigl.getName();
        id = aigl.getId();
        accountGroup = aigl.getAccountGroup();
        companyCode = aigl.getCompanyCode();
        ruleSet1 = aigl.getRuleSet1();
        autoClear = aigl.getAutoClear();
        suggestedClear = aigl.getSuggestedClear();
        totalOpenItems = aigl.getTotalOpenItems();
        totalCollected = aigl.getTotalCollected();
    }

    private void addDt(DataTransformer dt)
    {
        if (ruleSet1 == 0) ruleSet1 = dt.getRuleSet1();
        autoClear += dt.getAutoClear();
        suggestedClear += dt.getSuggestedClear();
        totalCollected += autoClear + suggestedClear;
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
    public int getTotalCollected()
    {
        return totalCollected;
    }
    public int getTotalOpenItems()
    {
        return totalOpenItems;
    }
    public void collectChildren(SchedulerSession session,PrintWriter p, Job job)
            throws Exception
    {
        scan(session,j,p,job);
    }
    private void scan(SchedulerSession session,Job parent,PrintWriter p, Job job)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {
            if(relevantJob(parent,child))
            {
                p.println("Account Item - scan found A " + child.getJobId());
                DataTransformer dt = new DataTransformer(session,child,true,p,job,companyCode,accountGroup,"conditional",parentDate);
                addDt(dt);
                dt = null;
            }
            else if(relevantJobB(child))
            {
                p.println("Account Item - scan found B " + child.getJobId());
                DataTransformer tot = new DataTransformer(session,child,true,p,job,companyCode,accountGroup,"conditional",parentDate);
                totalOpenItems = tot.getRuleSet1();
                //totalOpenItemsSet = tot.getTotalCollectedSet();
                tot = null;
            }
            else if(relevantJobC(child))
            {
                p.println("Account Item - scan found C " + child.getJobId());
                DataTransformer dt = new DataTransformer(session,child,false,p,job,companyCode,accountGroup,"conditional",parentDate);
                totalCollected = dt.getTotalCollected();
                //totalCollectedSet = dt.getTotalCollectedSet();
                dt = null;
            }
            scan(session,child,p,job);
        }
    }
    boolean relevantJob(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getName();
        String c = child.getJobDefinition().getName();
        return (p.equals("CUS_SPD_BSC_SUGGESTEDCLEAR_RULES_WEA_new") && c.startsWith("CUS_DT"));
    }
    boolean relevantJobB(Job child)
    {
        String c = child.getJobDefinition().getName();
        return c.contains("BaseWorking");
    }
    boolean relevantJobC(Job child)
    {
        String c = child.getJobDefinition().getName();
        return c.equals("CUS_TRN_COLLECT_RTX");
    }
}
