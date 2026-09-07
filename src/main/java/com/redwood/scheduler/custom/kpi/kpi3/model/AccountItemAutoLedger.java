package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;

import java.io.PrintWriter;
import java.util.Set;

class AccountItemAutoLedger implements AccountItemSource
{

    private static final Set<String> LEDGER_PATHS = Set.of("FCA_SAP_Generic_LoopCUS_SPD_BSC_AUTOCLEAR_SHERPAX_LDGRP", "FCA_SAP_Generic_LoopCUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_LDGRP_US");

    private final AccountItemContext context;
    private final CollectorStats stats;


    public AccountItemAutoLedger(Job job,DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(job, parentDate);
        this.stats = new CollectorStats();
    }

    public void collectChildren(SchedulerSession session,PrintWriter p,Job job)
            throws Exception
    {
        scan(session, context.getJob(), p,job);
    }
    private void scan(SchedulerSession session, Job parent, PrintWriter p,Job job)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {

            if (isLedgerCollector(parent, child))
            {
                Ledger ledger = new Ledger(child, context.getParentDate());
                ledger.collectChildren(session, p, job);
                stats.add(ledger.getStats());
            }

            scan(session,child,p,job);
        }
    }


    private boolean isLedgerCollector(Job parent, Job child)
    {
        String key =
        parent.getJobDefinition().getMasterJobDefinition().getName() +
        child.getJobDefinition().getMasterJobDefinition().getName();

        return LEDGER_PATHS.contains(key);

    }

    @Override
    public AccountItemContext getContext()
    {
        return context;
    }

    @Override
    public CollectorStats getStats()
    {
        return stats;
    }
}