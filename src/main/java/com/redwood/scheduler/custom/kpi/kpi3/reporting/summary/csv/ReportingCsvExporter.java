package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.csv;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.repository.ReportingRepository;

import java.util.ArrayList;
import java.util.List;

public class ReportingCsvExporter
{
    private final ReportingRepository repository = new ReportingRepository();

    public void export(
            SchedulerSession session,
            Job collectorJob,
            String period,
            String fileName)
            throws Exception
    {

        List<ReportingRecord> records = repository.findByPeriod(session, period);

        if (records.isEmpty()) return;

        List<String> content = new ArrayList<>();
        content.add(ReportingCsvSerializer.getHeader());

        for (ReportingRecord row : records) {
            if (row.isEmpty()) continue;
            content.add(ReportingCsvSerializer.toCsv(row));
        }

        new ResultFileWriter().writeItemsToNamedFile(session, collectorJob, fileName, content);

    }
}
