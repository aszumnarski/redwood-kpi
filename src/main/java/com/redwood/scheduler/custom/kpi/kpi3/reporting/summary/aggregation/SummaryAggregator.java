package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.aggregation;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.job.UserJobContext;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.date.PeriodUtil;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileAllocation;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.JobReportingRepository;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.csv.ReportingCsvSerializer;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingKey;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.repository.ReportingRepository;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import static com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.aggregation.VolumeAggregator.ENCODING;

public class SummaryAggregator {

    private final Set<String> processedFiles = new HashSet<>();
    private final Map<String, Integer> duplicatedReads = new HashMap<>();

    public void collect(SchedulerSession session, Job collectorJob, UserJobContext jobContext, Logger log) throws Exception {

        DateTimeZone runStart = collectorJob.getJobParameterByName("IN_DATE_FROM").getInValueDate();
        String period = PeriodUtil.getPeriod(runStart);
        Set<JobReportingRecord> records = new JobReportingRepository().findByPeriod(session, period);

        Map<ReportingKey, AggregationRecord> aggregatedData = new HashMap<>();

        for (JobReportingRecord record : records) {

            if (isEmpty(record)) continue;

            ReportingKey key = getKeyFromRecord(record);
            AggregationRecord aggregationRecord = aggregatedData.computeIfAbsent(key, AggregationRecord::new);
            add(session, aggregationRecord, record, log);

        }

        if (aggregatedData.isEmpty()) return;

        persistSummary(session, aggregatedData.values());

        String fileName = JobParameterHelper.getParameter(collectorJob, "IN_FILENAME");

        List<String> content = new ArrayList<>();
        content.add(ReportingCsvSerializer.getHeader());

        for (ReportingRecord row : aggregatedData.values().stream().map(AggregationRecord::toReportingRecord).toList()) {
            if (row.isEmpty()) continue;
            content.add(ReportingCsvSerializer.toCsv(row));
        }

        if (!duplicatedReads.isEmpty())
        {
            log.info("Duplicate file references: " + duplicatedReads);
        }

        new ResultFileWriter().writeItemsToNamedFile(session, collectorJob, fileName, content);


        jobContext.setJobOutputParameter("OUT_FILE_NAME", fileName);
        jobContext.setJobOutputParameter("OUT_FILE_SOURCE", jobContext.getJobId() + ":" + fileName);


    }

    private void persistSummary(SchedulerSession session, Collection<AggregationRecord> records)
            throws Exception
    {
        ReportingRepository repository = new ReportingRepository();

        for (AggregationRecord aggregation : records)
        {
            ReportingRecord summary = aggregation.toReportingRecord();

            if (summary.isEmpty()) continue;

            repository.save(session, summary);
        }
    }

    private void add(SchedulerSession session, AggregationRecord summary, JobReportingRecord record, Logger log) throws Exception {

        FileAllocation allocation = record.getSourceFiles();
        if (allocation.hasAnyKpiFile()) {
            Set<String> open = getUniqueLines(session, allocation.getTotal());
            Set<String> proposed = getUniqueLines(session, allocation.getProposed());
            Set<String> cleared = getUniqueLines(session, allocation.getCleared());
            Set<String> errors = getUniqueLines(session, allocation.getErrors());

            validate(record, open, proposed, cleared, errors, log);

            summary.addOpen(open);
            summary.addProposed(proposed);
            summary.addCleared(cleared);
            summary.addError(errors);

            summary.addKPICounts(record.getCounts());

        }else {
            summary.setKPICounts(record.getCounts());
        }
    }

    private void validate(JobReportingRecord record, Set<String> open, Set<String> proposed, Set<String> cleared, Set<String> errors, Logger log) {
        if (record.getCounts().getOpen() != open.size()) {
            log.info("[OPEN MISMATCH] " + "jobId=" + record.getJobId() +
                    ", type=" + record.getClearingType() +
                    ", account=" + record.getAccountItem() +
                    ", record=" + record.getCounts().getOpen() +
                    ", files=" + open.size());
        }

        if (record.getCounts().getProposed() != proposed.size()) {
            log.info("[PROPOSED MISMATCH] " + "jobId=" + record.getJobId() +
                    ", type=" + record.getClearingType() +
                    ", account=" + record.getAccountItem() +
                    ", record=" + record.getCounts().getProposed() +
                    ", files=" + proposed.size());
        }

        if (record.getCounts().getCleared() != cleared.size()) {
            log.info("[CLEARED MISMATCH] " + "jobId=" + record.getJobId() +
                    ", type=" + record.getClearingType() +
                    ", account=" + record.getAccountItem() +
                    ", record=" + record.getCounts().getCleared() +
                    ", files=" + cleared.size());
        }

        if (record.getCounts().getErrors() != errors.size()) {
            log.info("[ERROR MISMATCH] " + "jobId=" + record.getJobId() +
                    ", type=" + record.getClearingType() +
                    ", account=" + record.getAccountItem() +
                    ", record=" + record.getCounts().getErrors() +
                    ", files=" + errors.size());
        }
    }


    private Set<String> getUniqueLines(SchedulerSession session, List<String> paths)
            throws Exception {
        Set<String> collected = new HashSet<>();
        for (String path : paths) {
            if (processedFiles.add(path)) {
                String[] parts = path.split(":", -1);
                JobFile jf = session.getJobByJobId(Long.parseLong(parts[0])).getJobFileByName(parts[1]);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(), ENCODING))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        collected.add(line);
                    }
                }
            } else {
                duplicatedReads.merge(path, 1, Integer::sum);
            }
        }
        return collected;
    }

    private boolean isEmpty(JobReportingRecord record) {
        return (!record.getSourceFiles().hasAnyKpiFile()) && (record.getCounts().isEmpty());
    }

    private ReportingKey getKeyFromRecord(JobReportingRecord record) {
        return new ReportingKey(record.getPeriod(), record.getCompanyCode(), record.getAccountItem(), record.getClearingType().name().toLowerCase());
    }
}