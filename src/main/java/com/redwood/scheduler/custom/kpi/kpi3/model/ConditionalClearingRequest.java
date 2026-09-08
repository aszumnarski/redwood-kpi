package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.file.ClearingResultProcessor;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.service.RTXService;

import java.io.PrintWriter;

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

    public ConditionalClearingRequest(Job j)
            throws Exception {
        this.context = new AccountItemContext(j, j.getRunStart());
        this.stats = new CollectorStats();
        this.fileWriter = new ResultFileWriter(context, "conditional");
        this.resultProcessor = new ClearingResultProcessor(fileWriter);
        suggestionId = getLink(j);

    }

    public void collectChildren(SchedulerSession session, PrintWriter p, Job job)
            throws Exception {
        scan(session, context.getJob(), p, job);
    }

    private void scan(SchedulerSession session, Job parent, PrintWriter p, Job job)
            throws Exception {
        for (Job child : parent.getChildJobs()) {
            String name = child.getJobDefinition().getMasterJobDefinition().getName();
            switch (name) {
                case JOB_EXCEL -> processExcel(session, child, p, job);
                case JOB_PREPARATION -> processPreparation(session, child, p, job);
                case JOB_FB05 -> processFb05(session, child, p, job);
                default -> scan(session, child, p, job);
            }
        }
    }

    private void processFb05(SchedulerSession session, Job child, PrintWriter p, Job job) throws Exception {
        ClearingResult result = resultProcessor.process(session, child, job, selectedForClear);
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

        fileWriter.writeItemsToFile(session,job,name,RTXService.getDocumentKeys(j,parameter,"DocumentNo", "Doc.Item", "FiscalYear"));

    }

    private Long getLink(Job j) {
        String inExcel = getParameter(j, "IN_FILE_FROM_EP");

        if (inExcel == null) {
            throw new RuntimeException("Missing IN_FILE_FROM_EP parameter for job " + j.getJobId() + " (" + j.getJobDefinition().getMasterJobDefinition().getName() + ")");
        }

        for (String part : inExcel.split("_", -1)) {
            if (part.startsWith("ProcessID")) {
                return Long.valueOf(part.replace("ProcessID", ""));
            }
        }

        return -1L;
    }
}