package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.rtx.RTXReader;
import com.redwood.scheduler.api.rtx.RTXRow;
import com.redwood.scheduler.custom.kpi.kpi3.service.RTXService;

import java.util.Set;
import java.util.HashSet;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec.getFileName;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;

class DataTransformer {
    private final AccountItemContext context;

    private Long id;
    private int ruleSet1;
    private int autoClear;
    private int suggestedClear;
    private int totalCollected;
    private int skippedCount = 0;
    private String companyCode;
    private String accountGroup;
    private String type;

    public DataTransformer(SchedulerSession session, Job j, boolean collectRuleSet1, PrintWriter p, Job extractorJob, String companyCode, String accountGroup, String type, DateTimeZone parentDate)
            throws Exception {
        this(session, j, collectRuleSet1, p, extractorJob, companyCode, accountGroup, type, parentDate, false);
    }

    public DataTransformer(SchedulerSession session, Job j, boolean collectRuleSet1, PrintWriter p, Job extractorJob, AccountItemContext context, String type, boolean suggestedClearFlag) throws Exception {
        this(session, j, collectRuleSet1, p, extractorJob, context.getCompanyCode(), context.getAccountGroup(), type, context.getParentDate(), suggestedClearFlag);
    }

    public DataTransformer(SchedulerSession session, Job j, boolean collectRuleSet1, PrintWriter p, Job extractorJob, String companyCode, String accountGroup, String type, DateTimeZone parentDate, boolean suggestedClearFlag)
            throws Exception {
        context = new AccountItemContext(j, parentDate);

        this.companyCode = companyCode;
        this.accountGroup = accountGroup;
        this.type = type;
        id = j.getJobId();
        p.println("DataTransformer created - " + id);

        if (!context.getStatus().equals("Completed")) {
            ruleSet1 = 0;
            autoClear = 0;
            suggestedClear = 0;
            totalCollected = 0;
        } else {
            ruleSet1 = j.getJobParameterByName("RuleSet1RowCount") != null ? j.getJobParameterByName("RuleSet1RowCount").getOutValueNumber().intValue() : 0;
            if (collectRuleSet1) printRuleSet1Items(session, j, p, extractorJob);
            autoClear = j.getJobParameterByName("AutoClearRowCount") != null ? j.getJobParameterByName("AutoClearRowCount").getOutValueNumber().intValue() : 0;
            if (autoClear != 0) printAutoClearItems(session, j, p, extractorJob);
            suggestedClear = j.getJobParameterByName("SuggestedClearRowCount") != null ? j.getJobParameterByName("SuggestedClearRowCount").getOutValueNumber().intValue() : 0;
            if (suggestedClear != 0) printSuggestedClearItems(session, j, p, extractorJob);
            totalCollected = j.getJobParameterByName("OUT_ROWCOUNT") != null ? j.getJobParameterByName("OUT_ROWCOUNT").getOutValueNumber().intValue() : 0;
            if (totalCollected != 0 && !collectRuleSet1) printOpenItems(session, j, p, extractorJob);
            if (j.getJobParameterByName("SuggestedClearRowCount") == null) suggestedClear = totalCollected;
            if (suggestedClearFlag) printSetWithCondition(session, j, "RuleSet1", p, extractorJob, "proposed");
        }
    }

    private Set<String> getSuggestedClearItems(Job j, PrintWriter p)
            throws Exception {
        return getSet(j, "SuggestedClear", p);
    }

    private Set<String> getAutoClearItems(Job j, PrintWriter p)
            throws Exception {
        return getSet(j, "AutoClear", p);
    }

    private Set<String> getOpenItems(Job j, PrintWriter p)
            throws Exception {
        return getSet(j, "OUT_TABLE", p);
    }

    private Set<String> getRuleSet1Items(Job j, PrintWriter p)
            throws Exception {
        return getSet(j, "RuleSet1", p);
    }

    private Set<String> getSet(Job j, String parameter, PrintWriter p) throws Exception {

        String jobStatus = j.getStatus().getTranslationEN();
        if (!jobStatus.equals("Completed")) return null;

        if (j.getJobParameterByName(parameter).getOutValueTableParameter() == null) return new HashSet<>();
        Set<String> answer = new HashSet<>();
        try (RTXReader reader = j.getJobParameterByName(parameter).getOutValueTableParameter().getRTXReader()) {
            for (RTXRow r : reader.rows()) {
                String key = RTXService.getDocumentKey(r);
                if (answer.contains(key)) {
                    p.println(key + " already existst in set - skipping...");
                    skippedCount++;
                }
                answer.add(key);
            }
        }
        return answer;
    }

    private void printSuggestedClearItems(SchedulerSession session, Job j, PrintWriter p, Job extractorJob)
            throws Exception {
        printSet(session, j, "SuggestedClear", p, extractorJob, "proposed");
    }

    private void printAutoClearItems(SchedulerSession session, Job j, PrintWriter p, Job extractorJob)
            throws Exception {
        printSet(session, j, "AutoClear", p, extractorJob, "proposed");
    }

    private void printOpenItems(SchedulerSession session, Job j, PrintWriter p, Job extractorJob)
            throws Exception {
        printSet(session, j, "OUT_TABLE", p, extractorJob, "proposed");
    }

    private void printRuleSet1Items(SchedulerSession session, Job j, PrintWriter p, Job extractorJob)
            throws Exception {
        printSet(session, j, "RuleSet1", p, extractorJob, "total");
    }

    private void printSet(SchedulerSession session, Job j, String parameter, PrintWriter p, Job extractorJob, String name) throws Exception {

        String jobStatus = j.getStatus().getTranslationEN();
        if (!jobStatus.equals("Completed")) return;

        RTXReader reader = RTXService.getReader(j, parameter);
        if (reader == null) return;



        String fileName = getFileName(new FileKey(context.getParentDate(), companyCode, accountGroup, type, name));
        boolean append = true;
        JobFile jf = extractorJob.getJobFileByName(fileName);
        if (jf == null) {
            p.println("file missing: " + fileName);
            jf = createJobFile(session, extractorJob, fileName);
            append = false;
        }
        try (FileOutputStream out = new FileOutputStream(jf.getFileName(), append)) {
            try (reader) {
                for (RTXRow r : reader.rows()) {
                    String doc = RTXService.getDocumentKey(r);
                    write(out, doc);
                }
            }
        }
    }

    private void printSetWithCondition(SchedulerSession session, Job j, String parameter, PrintWriter p, Job extractorJob, String name)
            throws Exception {
        String jobStatus = j.getStatus().getTranslationEN();
        if (!jobStatus.equals("Completed")) return;
        if (j.getJobParameterByName(parameter) == null) return;
        if (j.getJobParameterByName(parameter).getOutValueTableParameter() == null) return;
        String fileName = getFileName(new FileKey(context.getParentDate(), companyCode, accountGroup, type, name));
        boolean append = true;
        JobFile jf = extractorJob.getJobFileByName(fileName);
        if (jf == null) {
            p.println("file missing: " + fileName);
            jf = createJobFile(session, extractorJob, fileName);
            append = false;
        }
        try (FileOutputStream out = new FileOutputStream(jf.getFileName(), append)) {
            try (RTXReader reader = j.getJobParameterByName(parameter).getOutValueTableParameter().getRTXReader()) {
                for (RTXRow r : reader.rows()) {
                    String autoComment = (r.getMetadata().hasColumn("Auto_comment")) ? r.getCanonicalStringValue("Auto_comment") : "";
                    String doc = RTXService.getDocumentKey(r);
                    if (autoComment.equals("Proposed for Clearing")) write(out, doc);
                }
            }
        }
    }

    public int getRuleSet1() {
        return ruleSet1;
    }

    public int getAutoClear() {
        return autoClear;
    }

    public int getSuggestedClear() {
        return suggestedClear;
    }

    public int getTotalCollected() {
        return totalCollected;
    }

    public Job getJob() {
        return context.getJob();
    }

}