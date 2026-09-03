package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getAccountGroups;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

public class AutoClearing
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

    public AutoClearing(Job j)
            throws Exception
    {
        this.j = j;
        id = j.getJobId();
        runStartDate = j.getRunStart();
        runEndDate = j.getRunEnd();
        status = j.getStatus().getTranslationEN();
        name = j.getJobDefinition().getName();
        accountGroups = getAccountGroups(j);
        companyCode = getParameter(j,"BUKRS");
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
            if(relevantJob(parent,child))
            {
                p.println("Account Item Auto - scan found 1 " + child.getJobId());
                AccountItemAuto ai = new AccountItemAuto(child,runStartDate);
                ai.collectChildren(session,p,job);
                addAi(ai,p);
                ai = null;
            }
            else if(relevantJob2(parent,child))
            {
                p.println("Account Item Auto - scan found 2 " + child.getJobId());
                AccountItemAutoOld aio = new AccountItemAutoOld(child,runStartDate);
                aio.collectChildren(session,p,job);
                AccountItemAuto ai = new AccountItemAuto(aio);
                addAi(ai,p);
                ai = null;
            }
            else if(relevantJob3(parent,child))
            {
                p.println("Account Item Auto - scan found 3 " + child.getJobId());
                AccountItemAutoLedger ail = new AccountItemAutoLedger(child,runStartDate);
                ail.collectChildren(session,p,job);
                AccountItemAuto ai = new AccountItemAuto(ail);
                addAi(ai,p);
                ail = null;
                ai = null;
            }
            else if(relevantJob4(parent,child))
            {
                p.println("Account Item Auto - scan found 4 " + child.getJobId());
                AccountItemAuto ai = new AccountItemAuto(child,runStartDate);
                ai.collectChildren(session,p,job);
                addAi(ai,p);
                ai = null;
            }
            else if(relevantJob5(parent,child))
            {
                p.println("Account Item Auto - scan found 5 " + child.getJobId());
                AccountItemAutoOld aio = new AccountItemAutoOld(child,runStartDate);
                aio.collectChildren(session,p,job);
                AccountItemAuto ai = new AccountItemAuto(aio);
                addAi(ai,p);
                aio = null;
                ai = null;
            }
            else if(relevantJob6(parent,child))
            {
                p.println("Account Item Auto - scan found 5 " + child.getJobId());
                AccountItemAutoOld aio = new AccountItemAutoOld(child,runStartDate);
                aio.collectChildren(session,p,job);
                AccountItemAuto ai = new AccountItemAuto(aio);
                addAi(ai,p);
                ai = null;
            }
            else if(relevantJob7(parent,child))
            {
                p.println("Account Item Auto - scan found 7 " + child.getJobId());
                AccountItemAutoLedger ail = new AccountItemAutoLedger(child,runStartDate);
                ail.collectChildren(session,p,job);
                AccountItemAuto ai = new AccountItemAuto(ail);
                addAi(ai,p);
                ai = null;
            }
            else if(relevantJob8(parent,child))
            {
                p.println("Account Item Auto - scan found 8 " + child.getJobId());
                AccountItemAuto ai = new AccountItemAuto(child,runStartDate);
                ai.collectChildren(session,p,job);
                addAi(ai,p);
                ai = null;
            }
            else if(relevantJob9(parent,child))
            {
                p.println("Account Item Auto - scan found 9 " + child.getJobId());
                AccountItemAuto ai = new AccountItemAuto(child,runStartDate);
                ai.collectChildren(session,p,job);
                addAi(ai,p);
                ai = null;
            }
            else if(relevantJob10(parent,child))
            {
                p.println("Account Item Auto - scan found 10 " + child.getJobId());
                AccountItemAuto ai = new AccountItemAuto(child,runStartDate);
                ai.collectChildren(session,p,job);
                addAi(ai,p);
                ai = null;
            }
            scan(session,child,p,job);
        }
    }

    boolean relevantJob(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //jcsOut.println(p+"___________"+c);
        return (p + c).equals("CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_WEACUS_TD_BSC_AUTOCLEAR_WEA");
    }

    boolean relevantJob2(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //jcsOut.println(p+"___________"+c);
        return (p).equals("CUS_SPD_BSC_AUTOCLEAR_RULES_WEA") && !(c).equals("CUS_SPD_BSC_AUTOCLEAR_RULES_WEA");
    }

    boolean relevantJob3(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //jcsOut.println(p+"___________"+c);
        return (p + c).equals("CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_SHERPAX_LDGRPCUS_TD_BSC_AUTOCLEAR_SHERPAX_LDGRP");
    }

    boolean relevantJob4(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //jcsOut.println(p+"___________"+c);
        return (p + c).equals("CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_SHERPAX_newCUS_TD_BSC_AUTOCLEAR_SHERPAX_new");
    }

    boolean relevantJob5(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //jcsOut.println(p+"___________"+c);
        return (p + c).equals("CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_byAccountItemCUS_TD_BSC_AUTOCLEAR_CB_IN");
    }

    boolean relevantJob6(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //jcsOut.println(p+"___________"+c);
        return (p + c).equals("CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_byScheduleCUS_TD_BSC_AUTOCLEAR_CB_IN");
    }

    boolean relevantJob7(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //jcsOut.println(p+"___________"+c);
        return (p + c).equals("CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASHCUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH");
    }

    boolean relevantJob8(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //jcsOut.println(p+"___________"+c);
        return (p + c).equals("CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_OriginalCUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_Original");
    }

    boolean relevantJob9(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //jcsOut.println(p+"___________"+c);
        return (p + c).equals("CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_SOFOMCUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH");
    }

    boolean relevantJob10(Job parent,Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();
        //jcsOut.println(p+"___________"+c);
        return (p + c).equals("CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_USCUS_TD_BSC_AUTOCLEAR_Cashmatching_E1P_US");
    }

    private void addAi(AccountItemAuto ai,PrintWriter p)
    {
        totalOpenItems += ai.getTotalOpenItems();
        autoClear += ai.getAutoClear();
        suggestedClear += ai.getSuggestedClear();
        itemsCleared += ai.getItemsCleared();
        selectedForClear += ai.getTotalCollected();
        errors += ai.getErrors();
    }
}