package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.file.ClearingResultProcessor;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.ScanStats;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXSchemas;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXService;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

public class ConditionalClearingRequest {

    private static final String JOB_EXCEL = "CUS_ConvertExcel2CSV2RTX_NoReplaceAll";
    private static final String JOB_PREPARATION = "CUS_DT_BSC_CONDITIONAL_AUTOCLEAR_Clearing";
    private static final String JOB_FB05 = "FCA_SAP_Tran_FB05_Clearing";

    private Long suggestionId;
    private Long excelId;
    private Long prepId;

    private int selectedForClear = 0;
    private final AccountItemContext context;
    private final CollectorStats stats;
    private final ResultFileWriter fileWriter;
    private final ClearingResultProcessor resultProcessor;
    private final ScanStats scanStats;
    private final Map<String, Long> sourceTimes = new HashMap<>();
    private final Map<String, Integer> sourceCounts = new HashMap<>();
    private int documentsWritten;

    public ConditionalClearingRequest(Job j)
            throws Exception {
        this.context = new AccountItemContext(j, j.getRunStart());
        this.stats = new CollectorStats();
        this.scanStats = new ScanStats();
        this.fileWriter = new ResultFileWriter(context, "conditional");
        this.resultProcessor = new ClearingResultProcessor(fileWriter, RTXSchemas.CONDITIONAL);
        suggestionId = getLink(j);

    }

    public void collectChildren(SchedulerSession session, PrintWriter p, Job job)
            throws Exception {
        scan(session, context.getJob(), p, job);
        p.println(
                "ConditionalClearingRequest stats: " +
                        scanStats +
                        ",\ndocumentsWritten=" + documentsWritten +
                        ",\ntotalOpenItems=" + stats.getTotalOpenItems() +
                        ",\nselectedForClear=" + selectedForClear
        );
        p.println("SOURCE PERFORMANCE");

        sourceTimes.entrySet()
                .stream()
                .sorted((a, b) ->
                        Long.compare(
                                b.getValue(),
                                a.getValue()))
                .forEach(e -> {

                    String sourceName = e.getKey();
                    long totalMs = e.getValue();
                    int count = sourceCounts.get(sourceName);

                    p.println(
                            sourceName +
                                    " | count=" + count +
                                    " | totalMs=" + totalMs +
                                    " | avgMs=" + (totalMs / count));
                });
    }

    private void scan(SchedulerSession session, Job parent, PrintWriter p, Job job)
            throws Exception {
        for (Job child : parent.getChildJobs()) {
            scanStats.incrementVisitedJobs();
            scanStats.jobIds(child.getJobId());
            String name = child.getJobDefinition().getMasterJobDefinition().getName();
            switch (name) {
                case JOB_EXCEL -> {
                    scanStats.excelJob();
                    long start = System.currentTimeMillis();
                    processExcel(session, child, p, job);
                    long elapsed = System.currentTimeMillis() - start;
                    sourceTimes.merge("EXCEL", elapsed, Long::sum);
                    sourceCounts.merge("EXCEL", 1, Integer::sum);
                }
                case JOB_PREPARATION -> {
                    scanStats.preparationJob();
                    long start = System.currentTimeMillis();
                    processPreparation(session, child, p, job);
                    long elapsed = System.currentTimeMillis() - start;
                    sourceTimes.merge("PREP", elapsed, Long::sum);
                    sourceCounts.merge("PREP", 1, Integer::sum);
                }
                case JOB_FB05 -> {
                    scanStats.fb05Job();
                    long start = System.currentTimeMillis();
                    processFb05(session, child, p, job);
                    long elapsed = System.currentTimeMillis() - start;
                    sourceTimes.merge("FB05", elapsed, Long::sum);
                    sourceCounts.merge("FB05", 1, Integer::sum);
                }
                default -> scan(session, child, p, job);
            }
        }
    }

    private void processFb05(SchedulerSession session, Job child, PrintWriter p, Job job) throws Exception {
        ClearingResult result = resultProcessor.process(session, child, job, selectedForClear,p);
        stats.apply(result);
    }

    private void processPreparation(SchedulerSession session, Job child, PrintWriter p, Job job) throws Exception {
        if (prepId == null) {
            prepId = child.getJobId();
        } else {
            p.println("Prep dt file 2x in chain " + child.getJobId());
        }
        String selectedStatus = child.getStatus().getTranslationEN();
        String prepClearing = getParameter(child, "PrepClearingRowCount");
        if (selectedStatus.equals("Completed")) {
            selectedForClear = Integer.parseInt(prepClearing);
            if (selectedForClear > 0) printSet(session, child, "PrepClearing", p, job, "proposed");
        }
    }

    private void processExcel(SchedulerSession session, Job child, PrintWriter p, Job job) throws Exception {
        if (excelId == null) {
            excelId = child.getJobId();
        } else {
            throw new Exception("Excel file 2x in chain " + child.getJobId());
        }
        String excelStatus = child.getStatus().getTranslationEN();
        String outLines = getParameter(child, "OUT_LINES");
        if (excelStatus.equals("Completed")) {
            stats.setTotalOpenItems(Integer.parseInt(outLines));
            if (stats.getTotalOpenItems() > 0) printSet(session, child, "OUT_RTX", p, job, "total");
        }
    }

    private void printSet(SchedulerSession session, Job j, String parameter, PrintWriter p, Job job, String name) throws Exception {
        if (!"Completed".equals(j.getStatus().getTranslationEN())) return;

        Set<String> keys = RTXService.getDocumentKeys(j,parameter,RTXSchemas.CONDITIONAL);
        fileWriter.writeItemsToFile(session,job,name,keys);
        documentsWritten += keys.size();

    }

    private Long getLink(Job j) {
        String inExcel = getParameter(j, "IN_FILE_FROM_EP");

        if (inExcel == null) {
            return -1L;
            //throw new RuntimeException("Missing IN_FILE_FROM_EP parameter for job " + j.getJobId() + " (" + j.getJobDefinition().getMasterJobDefinition().getName() + ")");
        }

        for (String part : inExcel.split("_", -1)) {
            if (part.startsWith("ProcessID")) {
                return Long.valueOf(part.replace("ProcessID", ""));
            }
        }

        return -1L;
    }
}