package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.repository;

import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.interfaces.FileParameterRuntime;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingKey;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HistoricalSummaryImporter
{
    private final ReportingRepository repository = new ReportingRepository();

    public void importCsv(SchedulerSession session, JobFile csv) throws Exception
    {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8)))
        {
            br.readLine(); // header

            String line;

            while ((line = br.readLine()) != null)
            {
                String[] cols = line.split(";", -1);

                ReportingKey key =
                        new ReportingKey(
                                cols[0], // period
                                cols[1], // company code
                                cols[7], // account group
                                cols[2]  // type
                        );

                ReportingRecord record = new ReportingRecord(key);

                record.getCounts().setOpen(Integer.parseInt(cols[3]));
                record.getCounts().setProposed(Integer.parseInt(cols[4]));
                record.getCounts().setCleared(Integer.parseInt(cols[5]));
                record.getCounts().setErrors(Integer.parseInt(cols[6]));

                repository.save(session, record);
            }
        }
    }
}
