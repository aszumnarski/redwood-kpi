package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.rtx.RTXReader;
import com.redwood.scheduler.api.rtx.RTXRow;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemContext;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.Stopwatch;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXService;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DataTransformerCollector {
    private final AccountItemContext context;
    private final CollectorStats stats;
    private final Job childJob;
    private final Job extractorJob;
    private final ResultFileWriter fileWriter;

    private final String type;

    public DataTransformerCollector(SchedulerSession session, Job childJob, boolean collectRuleSet1, PrintWriter p, Job extractorJob,
                                    AccountItemContext parentContext, ResultFileWriter fileWriter, String type,
                                    boolean suggestedClearFlag)
            throws Exception {

        this.context = parentContext;
        this.stats = new CollectorStats();
        this.type = type;
        this.childJob = childJob;
        this.extractorJob = extractorJob;
        this.fileWriter = fileWriter;
        long collectorStart = System.currentTimeMillis();

        //p.println("DataTransformerCollector created - " + childJob.getJobId());

        if (!JobParameterHelper.isCompleted(childJob)) return;

        //stats.setRuleSet1(JobParameterHelper.getIntParameter(childJob, "RuleSet1RowCount"));
        //if (collectRuleSet1) printSet(session, "RuleSet1", p, "total", false);
        if (collectRuleSet1) stats.setOpen(printSet(session, "RuleSet1", p, "total", false));
        //stats.setAutoClear(JobParameterHelper.getIntParameter(childJob, "AutoClearRowCount"));
        //if (stats.getAutoClear() != 0) printSet(session, "AutoClear", p, "proposed", false);
        stats.addProposed(printSet(session, "AutoClear", p, "proposed", false));
        //stats.setSuggestedClear(JobParameterHelper.getIntParameter(childJob, "SuggestedClearRowCount"));
        //if (stats.getSuggestedClear() != 0) printSet(session, "SuggestedClear", p, "proposed", false);
        stats.addProposed(printSet(session, "SuggestedClear", p, "proposed", false));

        int totalCollected = JobParameterHelper.getIntParameter(childJob, "OUT_ROWCOUNT");

        if (totalCollected != 0 && !collectRuleSet1)
            printSet(session, "OUT_TABLE", p, "proposed", false);

        //if (!JobParameterHelper.hasParameter(childJob, "SuggestedClearRowCount"))
        //    stats.setSuggestedClear(stats.getOpen());
        if (suggestedClearFlag) stats.setProposed(printSet(session, "RuleSet1", p, "proposed", true));

        p.println(
                "DataTransformerCollector[" + childJob.getJobId() + "]" +
                        " totalMs=" + (System.currentTimeMillis() - collectorStart) +
                        " counts=" + stats.getCounts()
        );

    }

    private int printSet(SchedulerSession session, String parameter, PrintWriter p, String name, boolean proposedOnly) throws Exception {
        int result = 0;

        try (RTXReader reader = RTXService.getReader(childJob, parameter)) {
            if (reader == null) return result;
            long rowsRead = 0;
            long docsWritten = 0;
            long totalStart = System.currentTimeMillis();

            Stopwatch stopwatch = new Stopwatch();

            List<String> documentKeys = new ArrayList<>();
            for (RTXRow r : reader.rows()) {
                rowsRead++;
                String documentKey = RTXService.getDocumentKey(r);
                if (proposedOnly) {
                    String autoComment = (r.getMetadata().hasColumn("Auto_comment")) ? r.getCanonicalStringValue("Auto_comment") : "";
                    if (!"Proposed for Clearing".equals(autoComment)) continue;
                }
                documentKeys.add(documentKey);
                docsWritten++;
            }
            long keyMs = stopwatch.elapsedMs();
            stopwatch.reset();
            result = fileWriter.writeItemsToFile(session, extractorJob, name, documentKeys);
            long writeMs = stopwatch.elapsedMs();
            long totalElapsedMs = System.currentTimeMillis() - totalStart;
            long otherMs = totalElapsedMs - keyMs - writeMs;

            p.println("DT[" + childJob.getJobId() + "] " + parameter + " rows=" + rowsRead + " docs=" + docsWritten + " totalMs=" + totalElapsedMs + " keyMs=" + keyMs + " writeMs=" + writeMs + " otherMs=" + otherMs);

        }
        return result;
    }

    public int getOpen() {
        return stats.getCounts().getOpen();
    }

    public Job getJob() {
        return childJob;
    }

    public CollectorStats getStats() {
        return stats;
    }

}