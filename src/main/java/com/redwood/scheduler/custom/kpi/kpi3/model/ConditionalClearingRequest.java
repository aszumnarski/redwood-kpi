package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.enumeration.JobStatus;
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



public class ConditionalClearingRequest {

    private static final String JOB_EXCEL = "CUS_ConvertExcel2CSV2RTX_NoReplaceAll";
    private static final String JOB_FB05 = "FCA_SAP_Tran_FB05_Clearing";

    private long excelId;
    private long clearingId;

    private final AccountItemContext context;
    private final CollectorStats stats;
    private final ResultFileWriter fileWriter;
    private final ClearingResultProcessor resultProcessor;
    private final ScanStats scanStats;
    private final Map<String, Long> sourceTimes = new HashMap<>();
    private final Map<String, Integer> sourceCounts = new HashMap<>();


    public ConditionalClearingRequest(Job j)
            throws Exception {
        this.context = new AccountItemContext(j, j.getRunStart());
        this.stats = new CollectorStats();
        this.scanStats = new ScanStats();
        this.fileWriter = new ResultFileWriter(context, "conditional");
        this.resultProcessor = new ClearingResultProcessor(fileWriter, RTXSchemas.CONDITIONAL);

    }

    public void collectChildren(SchedulerSession session, PrintWriter p, Job job)
            throws Exception {
        scan(session, context.getJob(), p, job);
        p.println(
                "ConditionalClearingRequest stats: " +
                        scanStats +
                        ",\nStats=" + stats.getCounts()
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
        if (allSourcesFound()) return;
        for (Job child : parent.getChildJobs()) {
            if (allSourcesFound()) return;
            scanStats.incrementVisitedJobs();
            scanStats.jobIds(child.getJobId());
            if(child.getStatus() == JobStatus.Skipped) continue;
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

    private boolean allSourcesFound(){
        return excelId > 0 && clearingId > 0;
    }

    private void processFb05(SchedulerSession session, Job child, PrintWriter p, Job job) throws Exception {
        if (clearingId == 0) {
            clearingId = child.getJobId();
        } else {
            throw new Exception("Clearing job 2x in chain " + child.getJobId());
        }
        ClearingResult result = resultProcessor.process(session, child, job,p);
        stats.apply(result);
    }


    private void processExcel(SchedulerSession session, Job child, PrintWriter p, Job job) throws Exception {
        if (excelId == 0) {
            excelId = child.getJobId();
        } else {
            throw new Exception("Excel file 2x in chain " + child.getJobId());
        }

        if (JobStatus.Completed == child.getStatus()) {
            Set<String> keys = RTXService.getDocumentKeys(child,"OUT_RTX",RTXSchemas.CONDITIONAL);
            stats.setOpen(fileWriter.writeItemsToFile(session,job,"total",keys));
        }
    }

}