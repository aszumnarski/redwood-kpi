package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.custom.kpi.kpi3.config.AccountItemType;
import com.redwood.scheduler.custom.kpi.kpi3.config.AutoRule;
import com.redwood.scheduler.custom.kpi.kpi3.config.ExactMatchRule;
import com.redwood.scheduler.custom.kpi.kpi3.config.ParentDifferentChildRule;

import java.io.PrintWriter;
import java.util.List;

public class AutoClearing {
    private DateTimeZone runStartDate;
    private int totalOpenItems = 0;
    private int selectedForClear = 0;
    private int itemsCleared = 0;
    private int errors = 0;
    private int autoClear = 0;
    private int suggestedClear = 0;
    private Job j;

    private static final List<AutoRule> RULES = List.of(

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_WEA",
                    "CUS_TD_BSC_AUTOCLEAR_WEA",
                    AccountItemType.AUTO),

            new ParentDifferentChildRule(
                    "CUS_SPD_BSC_AUTOCLEAR_RULES_WEA",
                    AccountItemType.AUTO_OLD),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_SHERPAX_LDGRP",
                    "CUS_TD_BSC_AUTOCLEAR_SHERPAX_LDGRP",
                    AccountItemType.AUTO_LEDGER),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_SHERPAX_new",
                    "CUS_TD_BSC_AUTOCLEAR_SHERPAX_new",
                    AccountItemType.AUTO),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_byAccountItem",
                    "CUS_TD_BSC_AUTOCLEAR_CB_IN",
                    AccountItemType.AUTO_OLD),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_bySchedule",
                    "CUS_TD_BSC_AUTOCLEAR_CB_IN",
                    AccountItemType.AUTO_OLD),
            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH",
                    "CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH",
                    AccountItemType.AUTO_LEDGER),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_Original",
                    "CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_Original",
                    AccountItemType.AUTO),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_SOFOM",
                    "CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH",
                    AccountItemType.AUTO),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_US",
                    "CUS_TD_BSC_AUTOCLEAR_Cashmatching_E1P_US",
                    AccountItemType.AUTO)

    );

    public AutoClearing(Job j) {
        this.j = j;
        runStartDate = j.getRunStart();
    }

    public void collectActionItems(SchedulerSession session, PrintWriter p, Job job)
            throws Exception {
        scan(session, j, p, job);
    }

    private void scan(SchedulerSession session, Job parent, PrintWriter p, Job job)
            throws Exception {
        for (Job child : parent.getChildJobs()) {

            AutoRule rule = findRule(parent, child);

            if (rule != null)
            {
                processRule(rule, child, session, p, job);
            }

            scan(session, child, p, job);
        }
    }

    private void processRule(
            AutoRule rule,
            Job child,
            SchedulerSession session,
            PrintWriter p,
            Job job)
            throws Exception
    {
        switch (rule.type())
        {
            case AUTO:
            {
                AccountItemAuto ai = new AccountItemAuto(child, runStartDate);
                ai.collectChildren(session, p, job);
                addAi(ai, p);
                break;
            }

            case AUTO_OLD:
            {
                AccountItemAutoOld aio = new AccountItemAutoOld(child, runStartDate);
                aio.collectChildren(session, p, job);
                addAi(new AccountItemAuto(aio), p);
                break;
            }

            case AUTO_LEDGER:
            {
                AccountItemAutoLedger ail = new AccountItemAutoLedger(child, runStartDate);
                ail.collectChildren(session, p, job);
                addAi(new AccountItemAuto(ail), p);
                break;
            }
        }
    }

    private AutoRule findRule(Job parent, Job child)
    {
        for (AutoRule rule : RULES)
        {
            if (rule.matches(parent, child))
            {
                return rule;
            }
        }

        return null;
    }



    private void addAi(AccountItemAuto ai, PrintWriter p) {
        totalOpenItems += ai.getTotalOpenItems();
        autoClear += ai.getAutoClear();
        suggestedClear += ai.getSuggestedClear();
        itemsCleared += ai.getItemsCleared();
        selectedForClear += ai.getTotalCollected();
        errors += ai.getErrors();
    }
}