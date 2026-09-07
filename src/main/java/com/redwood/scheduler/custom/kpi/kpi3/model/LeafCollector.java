package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.file.ClearingResultProcessor;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;

import java.io.PrintWriter;

class LeafCollector implements AccountItemSource
{
    private final AccountItemContext context;
    private final ResultFileWriter fileWriter;
    private final ClearingResultProcessor clearingProcessor;
    private final CollectorConfig config;
    private final CollectorStats stats;

    public LeafCollector(
            Job job,
            DateTimeZone parentDate,
            CollectorConfig config) throws Exception {

        this.context = new AccountItemContext(job, parentDate);
        this.fileWriter = new ResultFileWriter(context, "auto");
        this.clearingProcessor = new ClearingResultProcessor(fileWriter);
        this.config = config;
        this.stats = new CollectorStats();
    }

    public void collectChildren(SchedulerSession session, PrintWriter p, Job job)
            throws Exception
    {
        scan(session, context.getJob(), p,job);
    }
    private void scan(SchedulerSession session,Job parent,PrintWriter pw,Job job)
            throws Exception
    {

        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        for(Job child: parent.getChildJobs())
        {
            String c = child.getJobDefinition().getMasterJobDefinition().getName();
            if(config.baseParents().contains(p) && (c).contains("BaseWorking"))
            {
                DataTransformer tot = new DataTransformer(session,child,true,pw,job, context.getCompanyCode(), context.getAccountGroup(), "auto",context.getParentDate());
                stats.setTotalOpenItems(tot.getRuleSet1());

            }
            if((!config.restrictDtParents() && c.startsWith("CUS_DT")) || (config.restrictDtParents() && config.dtParents().contains(p) && c.startsWith("CUS_DT")))
            {
                DataTransformer dt = new DataTransformer(session,child,false,pw,job,context.getCompanyCode(), context.getAccountGroup(),"auto",context.getParentDate());
                stats.addDt(dt);
            }
            else if(c.equals("CUS_TRN_COLLECT_RTX") && config.collectRtx())
            {
                DataTransformer dt = new DataTransformer(session,child,false,pw,job,context.getCompanyCode(), context.getAccountGroup(),"auto",context.getParentDate());
                stats.setTotalCollectedItems(dt.getTotalCollected());
            }
            else if(c.equals("FCA_SAP_Tran_FB05_Clearing"))
            {
                stats.apply(clearingProcessor.process(session, child, job, stats.getTotalCollected()));
            }
            scan(session,child,pw,job);
        }

    }

    public CollectorStats getStats()
    {
        return stats;
    }

    public AccountItemContext getContext()
    {
        return context;
    }

}
