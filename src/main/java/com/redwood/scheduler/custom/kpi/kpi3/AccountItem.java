package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;

import java.io.PrintWriter;

class AccountItem
{
    private int ruleSet1 = 0;
    private int autoClear = 0;
    private int suggestedClear = 0;
    private int totalCollected = 0;
    private int totalOpenItems = 0;

    private final AccountItemContext context;

    public AccountItem(Job job,DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(job, parentDate);
    }

    public AccountItem(AccountItemGL aigl)
    {
        this.context = aigl.getContext();
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
        scan(session, context.getJob(), p,job);
    }
    AccountItemContext getContext()
    {
        return context;
    }
    private void scan(SchedulerSession session,Job parent,PrintWriter p, Job job)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {
            if(relevantJob(parent,child))
            {
                p.println("Account Item - scan found A " + child.getJobId());
                DataTransformer dt = new DataTransformer(session,child,true,p,job, context.getCompanyCode(), context.getAccountGroup(), "conditional",context.getParentDate());
                addDt(dt);
            }
            else if(relevantJobB(child))
            {
                p.println("Account Item - scan found B " + child.getJobId());
                DataTransformer tot = new DataTransformer(session,child,true,p,job, context.getCompanyCode(), context.getAccountGroup(), "conditional",context.getParentDate());
                totalOpenItems = tot.getRuleSet1();
            }
            else if(relevantJobC(child))
            {
                p.println("Account Item - scan found C " + child.getJobId());
                DataTransformer dt = new DataTransformer(session,child,false,p,job, context.getCompanyCode(), context.getAccountGroup(), "conditional",context.getParentDate());
                totalCollected = dt.getTotalCollected();
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
