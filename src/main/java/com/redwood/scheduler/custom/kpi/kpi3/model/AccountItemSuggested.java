package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;

import java.io.PrintWriter;
import java.util.List;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

public class AccountItemSuggested implements AccountItemSource
{
    private static final List<String> BASE_PARENTS = List.of(
            "CUS_TD_BSC_SuggestedClearing_OneSided_MASTER_SHERPAX",
            "CUS_TD_BSC_SuggestedClearing_OneSided_MASTER");

    private final AccountItemContext context;
    private final CollectorStats stats;
    private final ResultFileWriter fileWriter;

    public AccountItemSuggested(
            Job job,
            DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(
                job,
                parentDate);

        this.stats = new CollectorStats();
        this.fileWriter = new ResultFileWriter(context, "suggested");
    }

    @Override
    public void collectChildren(
            SchedulerSession session,
            PrintWriter p,
            Job job)
            throws Exception
    {
        p.println("JOBSTATUS: " + context.getStatus());

        if (!"Waiting".equals(context.getStatus()) && !"Completed".equals(context.getStatus()))
        {
            return;
        }

        scan(session, context.getJob(), p, job);
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

    private void scan(
            SchedulerSession session,
            Job parent,
            PrintWriter p,
            Job job)
            throws Exception
    {
        String parentName = parent.getJobDefinition().getMasterJobDefinition().getName();

        for (Job child : parent.getChildJobs())
        {
            String childName = child.getJobDefinition().getMasterJobDefinition().getName();

            if (BASE_PARENTS.contains(parentName) && childName.contains("Certification_Process_Account"))
            {
                processCertification(session, child, p, job);
            }

            scan(session, child, p, job);
        }
    }

    private void processCertification(
            SchedulerSession session,
            Job child,
            PrintWriter p,
            Job job)
            throws Exception
    {
        String certificationId = getParameter(child, "CERT_UNIQUE_ID");

        if (certificationId == null)
        {
            return;
        }

        p.println("Cert ID: " + certificationId);

        fileWriter.writeItemsToFile(session, job, "certId",List.of(certificationId));
    }
}