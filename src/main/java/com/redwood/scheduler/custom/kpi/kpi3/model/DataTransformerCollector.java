package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.rtx.RTXReader;
import com.redwood.scheduler.api.rtx.RTXRow;
import com.redwood.scheduler.custom.kpi.kpi3.service.RTXService;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec.getFileName;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.*;

class DataTransformerCollector {
    private final AccountItemContext context;
    private final CollectorStats stats;
    private final Job childJob;
    private final Job extractorJob;

    private final String type;


    public DataTransformerCollector(SchedulerSession session, Job childJob, boolean collectRuleSet1, PrintWriter p, Job extractorJob, AccountItemContext parentContext, String type, boolean suggestedClearFlag) throws Exception {

        this.context = parentContext;
        this.stats = new CollectorStats();
        this.type = type;
        this.childJob = childJob;
        this.extractorJob = extractorJob;

        p.println("DataTransformerCollector created - " + childJob.getJobId());

        if (!isCompleted(childJob)) return;

        stats.setRuleSet1(getIntParameter(childJob, "RuleSet1RowCount"));
        if (collectRuleSet1) printSet(session, "RuleSet1", p, "total", false);

        stats.setAutoClear(getIntParameter(childJob, "AutoClearRowCount"));
        if (stats.getAutoClear() != 0) printSet(session, "AutoClear", p, "proposed", false);

        stats.setSuggestedClear(getIntParameter(childJob, "SuggestedClearRowCount"));
        if (stats.getSuggestedClear() != 0) printSet(session, "SuggestedClear", p, "proposed", false);

        stats.setTotalCollectedItems(getIntParameter(childJob, "OUT_ROWCOUNT"));
        if (stats.getTotalCollected() != 0 && !collectRuleSet1)
            printSet(session, "OUT_TABLE", p, "proposed", false);

        if (!hasParameter(childJob, "SuggestedClearRowCount")) stats.setSuggestedClear(stats.getTotalCollected());
        if (suggestedClearFlag) printSet(session, "RuleSet1", p, "proposed", true);

    }

    private void printSet(SchedulerSession session, String parameter, PrintWriter p, String name, boolean proposedOnly) throws Exception {

        try (RTXReader reader = RTXService.getReader(childJob, parameter)) {
            if (reader == null) return;

            String fileName = getFileName(new FileKey(context.getParentDate(), context.getCompanyCode(), context.getAccountGroup(), type, name));
            boolean append = true;
            JobFile jf = extractorJob.getJobFileByName(fileName);
            if (jf == null) {
                p.println("file missing: " + fileName);
                jf = createJobFile(session, extractorJob, fileName);
                append = false;
            }
            try (FileOutputStream out = new FileOutputStream(jf.getFileName(), append)) {

                for (RTXRow r : reader.rows()) {
                    if (proposedOnly) {
                        String autoComment = (r.getMetadata().hasColumn("Auto_comment")) ? r.getCanonicalStringValue("Auto_comment") : "";
                        if (!"Proposed for Clearing".equals(autoComment)) continue;
                    }

                    write(out, RTXService.getDocumentKey(r));
                }

            }
        }
    }

    public int getRuleSet1() {
        return stats.getRuleSet1();
    }

    public int getAutoClear() {
        return stats.getAutoClear();
    }

    public int getSuggestedClear() {
        return stats.getSuggestedClear();
    }

    public int getTotalCollected() {
        return stats.getTotalCollected();
    }

    public Job getJob() {
        return childJob;
    }

    public CollectorStats getStats() {
        return stats;
    }

}