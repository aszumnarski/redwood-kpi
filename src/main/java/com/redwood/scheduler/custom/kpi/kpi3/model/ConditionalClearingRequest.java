package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.enumeration.JobStatus;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.file.ClearingResultProcessor;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.ScanStats;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.factory.JobReportingRecordFactory;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.JobReportingRepository;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXSchemas;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXService;

import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ConditionalClearingRequest {

    private static final Pattern LINK_ID_PATTERN = Pattern.compile("ProcessID(\\d+)");

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

    private final JobReportingRecordFactory factory;
    private final JobReportingRepository repository;

    private final String userId;

    private final Long linkId;


    public ConditionalClearingRequest(Job j)
            throws Exception {
        this.context = new AccountItemContext(j, j.getRunStart());
        this.stats = new CollectorStats();
        this.scanStats = new ScanStats();
        this.fileWriter = new ResultFileWriter(context, "conditional");
        this.resultProcessor = new ClearingResultProcessor(fileWriter, RTXSchemas.CONDITIONAL);

        this.factory = new JobReportingRecordFactory();
        this.repository = new JobReportingRepository();

        this.userId = JobParameterHelper.getParameter(j,"MX_To");

        this.linkId = setLinkId(JobParameterHelper.getParameter(j,"IN_FILE_FROM_EP"));

    }

    private Long setLinkId(String fileName) {

            if (fileName == null || fileName.isEmpty())
            {
                return null;
            }

            Matcher matcher = LINK_ID_PATTERN.matcher(fileName);

            if (matcher.find())
            {
                return Long.parseLong(matcher.group(1));
            }

            return null;


            //TODO: add fallback = get from RTX from CUS_ConvertExcel2CSV2RTX_NoReplaceAll (Step 3 - ConvertRTX)

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

        JobReportingRecord record = factory.create(session,context, stats, fileWriter.getAllocation(), JobType.CONDITIONAL, stringToSet(userId),linkId);

        p.println("JOB REPORTING RECORD");
        p.println(record);

        repository.save(session, record);
    }

    private static Set<String> stringToSet(String input)
    {
        if (input == null || input.isBlank())
        {
            return Collections.emptySet();
        }

        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
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

    public String getUserId() {
        return userId;
    }

    public Long getLinkId() {
        return linkId;
    }
}