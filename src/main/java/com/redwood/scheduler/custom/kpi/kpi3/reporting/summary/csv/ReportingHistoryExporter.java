package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.csv;

import com.redwood.scheduler.api.exception.ParameterDefaultValueUnavailableException;
import com.redwood.scheduler.api.exception.SchedulerAPIPersistenceException;
import com.redwood.scheduler.api.model.*;
import com.redwood.scheduler.api.model.enumeration.JobStatus;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.repository.ReportingRepository;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.util.*;

public class ReportingHistoryExporter
{
    private static final String EXPORTER_JOB_DEFINITION = "CUS_TD_KPI_MANUAL_UPLOAD_KPI3_HISTORY";
    private final ReportingRepository repository = new ReportingRepository();
    private final ResultFileWriter writer = new ResultFileWriter();

    public void execute(SchedulerSession session, Job collectorJob, Logger log) throws Exception {
        List<JobFile> files = exportAllPeriods(session,collectorJob,log);
        if(files.isEmpty()) return;

        for(JobFile file : files){
            exportJobFile(session,file,log);
        }
    }

    private void exportJobFile(SchedulerSession session, JobFile file,Logger log) throws ParameterDefaultValueUnavailableException, SchedulerAPIPersistenceException {
        JobDefinition definition = getExporterDefinition(session);
        if(definition == null) throw new NullPointerException(EXPORTER_JOB_DEFINITION + " does not exist on environment.");
        Job exporter = definition.prepare();
        JobParameter fileParameter = exporter.getJobParameterByName("IN_FILE");
        if(fileParameter == null) throw new NullPointerException("'IN_FILE' does not exist on " + EXPORTER_JOB_DEFINITION + ".");
        log.info("Uploading: " + file.getFileName());

        fileParameter.setInValueFileParameter(file);

        session.persist();

        session.waitForJob(exporter);

    }

    JobDefinition getExporterDefinition(SchedulerSession session)
    {
        Iterator<JobDefinition>it = session.executeObjectQuery(JobDefinition.TYPE,"select * from JobDefinition where Name = ? and MasterJobDefinition = UniqueId", EXPORTER_JOB_DEFINITION);
        return it.hasNext() ? it.next() : null;
    }

    private List<JobFile> exportAllPeriods(SchedulerSession session, Job collectorJob,Logger log)
            throws Exception
    {
        List<JobFile> result = new ArrayList<>();
        Set<String> periods = getPeriods(session);

        log.info("Found " + periods.size() + " periods to export.");
        log.info("Periods: " + periods);

        for (String period : periods)
        {
            log.info("Exporting period " + period);

            String fileName = "KPI3_" + period + ".csv";

            List<ReportingRecord> records = repository.findByPeriod(session, period);

            if (records.isEmpty()) continue;

            List<String> content = new ArrayList<>();
            content.add(ReportingCsvSerializer.getHeader());

            for (ReportingRecord row : records) {
                if (row.isEmpty()) continue;
                content.add(ReportingCsvSerializer.toCsv(row));
            }

            writer.writeItemsToNamedFile(session, collectorJob, fileName, content);
            result.add(collectorJob.getJobFileByName(fileName));

            log.info("Generated file " + fileName + " rows=" + content.size());
        }

        return result;
    }

    private Set<String> getPeriods(SchedulerSession session)
    {
        Set<String> result = new TreeSet<>();

        Iterator<Table> it = session.executeObjectQuery(Table.TYPE, "select * from Table where Name like ?", "KPI3_SUMMARY_REPORTING_%");

        while (it.hasNext())
        {
            Table table = it.next();
            String suffix = table.getName().replace("KPI3_SUMMARY_REPORTING_", "");
            result.add("FY" + suffix.substring(0, 4) + "P" + suffix.substring(5));
        }

        return result;
    }
}
