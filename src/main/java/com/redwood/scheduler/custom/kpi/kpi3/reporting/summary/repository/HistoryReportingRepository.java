package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.repository;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.JobParameter;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.file.*;
import com.redwood.scheduler.custom.kpi.kpi3.model.KPICounts;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.aggregation.ReportingAggregator;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.csv.ReportingCsvSerializer;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.repository.ReconciliationRepository;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.*;

public class HistoryReportingRepository {

    private final SchedulerSession session;
    private final Job job;
    private final Logger log;

    public HistoryReportingRepository(SchedulerSession session, Job job, Logger log){
        this.session = session;
        this.job = job;
        this.log = log;
    }

    public void execute() throws Exception {

        Map<FileKey, List<String>> historicalFiles = getHistoricalFiles();

        ReportingAggregator aggregator = collectRows(historicalFiles);

        if (aggregator.getRecords().isEmpty()) return;


        String fileName = "history.csv";
        JobFile jf = JobFileService.createJobFile(session,job, fileName);
        try (FileOutputStream fos = new FileOutputStream(jf.getFileName()))
        {
            ResultFileWriter.write(fos, ReportingCsvSerializer.getHeader());
            for(ReportingRecord row:aggregator.getRecords())
            {
                if(row.isEmpty()) continue;
                ResultFileWriter.write(fos, ReportingCsvSerializer.toCsv(row));
            }
        }

    }


    Map<FileKey, List<String>> getHistoricalFiles(){
        Map<FileKey, List<String>> result = new HashMap<>();
        String query = "select jp.* from JobParameter jp join JobDefinitionParameter jdp on jp.JobDefinitionParameter = jdp.UniqueId join JobDefinition jd on jdp.JobDefinition = jd.UniqueId where jdp.Name = 'IN_FILES' and jd.Name in ('CUS_TRN_KPI_3_COLLECT_VOLUMES_BETA','CUS_TRN_KPI_3_COLLECT_VOLUMES_BETA_1')";
        Iterator<JobParameter> it = session.executeObjectQuery(JobParameter.TYPE,query);
        while(it.hasNext()){
            JobParameter jp = it.next();
            String paths = jp.getInValueString();
            log.info(paths);
            if (paths == null) continue;
            if(paths.length() < 10) continue;
            //1_period+FY2026P03__cc+5251__ag+BC14100_OthCurrFinAsset_CFAVF__type+conditional__name+total.csv=JobFile:82458155:period+FY2026P03__cc+5251__ag+BC14100_OthCurrFinAsset_CFAVF__type+conditional__name+total.csv;1_period+FY2026P03__cc+5251__ag+BC16100_OthFinAssets_CFAVF__type+conditional__name+total.csv=JobFile:82458155:period+FY2026P03__cc+5251__ag+BC16100_OthFinAssets_CFAVF__type+conditional__name+total.csv;1_period+FY2026P03__cc+5251__ag+BC36300_OtherLiabilitie_CFAVF__type+conditional__name+total.csv=JobFile:82458155:period+FY2026P03__cc+5251__ag+BC36300_OtherLiabilitie_CFAVF__type+conditional__name+total.csv;1_period+FY2026P03__cc+5251__ag+B055000_ShareholdEquity_CFRLO__type+conditional__name+total.csv=JobFile:82458155:period+FY2026P03__cc+5251__ag+B055000_ShareholdEquity_CFRLO__type+conditional__name+total.csv;1_period+FY2026P03__cc+5251__ag+B055000_ShareholdEquity_CFRLO__type+conditional__name+proposed.csv=JobFile:82458155:period+FY2026P03__cc+5251__ag+B055000_ShareholdEquity_CFRLO__t
            for(String path : paths.split(";",-1))
            {
                log.info("processing: " + path);
                String[] parts = path.split("=",-1);
                log.info("will produce path key from: " + parts[1]);
                String[] fields = parts[1].split(":",-1);
                //JobFile:82458155:period+FY2026P03__cc+5251__ag+BC14100_OthCurrFinAsset_CFAVF__type+conditional__name+total.csv
                FileKey fileKey = getKey(fields[2]);
                log.info("File key is:" + fileKey);
                result.computeIfAbsent(fileKey, k -> new ArrayList<>()).add(parts[1]);
            }
        }

        return result;
    }

    private ReportingAggregator collectRows(Map<FileKey, List<String>> filesMap) throws Exception {
        ReportingAggregator result = new ReportingAggregator();
        Set<String> missingChains = new HashSet<>();
        Set<String> processedConditionalFiles = new HashSet<>();


        for (FileKey key : filesMap.keySet())
        {
            log.info("collectRows looping key: " + key);

            FileAllocation allocation = new FileAllocation();

            for (String path : filesMap.get(key))
            {
                log.info("Allocating path: " + path);
                allocation.add(path);
            }

            if (!allocation.hasAnyKpiFile())
            {
                if (!allocation.getMissings().isEmpty())
                {
                    missingChains.addAll(getUniqueLines(allocation.getMissings()));
                }
                continue;
            }

            log.info("conditional.isEmpty()=" + allocation.getConditional().isEmpty());
            if (!allocation.getConditional().isEmpty())
            {
                String conditionalFile = allocation.getConditional().get(0);

                if (processedConditionalFiles.add(conditionalFile))
                {
                    parseConditionalFile(allocation.getConditional(), result);
                }

                if (!allocation.getMissings().isEmpty())
                {
                    missingChains.addAll(getUniqueLines(allocation.getMissings()));
                }
                log.info("====================================================================================");
                continue;
            }

            ReportingRecord row = new ReportingRecord(key);

            log.info("certId.isEmpty()=" +  allocation.getCertId().isEmpty());
            if (!allocation.getCertId().isEmpty())
            {
                Set<String> certificationSet = getUniqueLines(allocation.getCertId());
                if(certificationSet.size() != 1)
                {
                    log.error("More than 1 certification found! " + certificationSet + " for key " + key);
                    //throw new Exception ("More than 1 certification found! " + certificationSet + " for key " + key);
                }
                String id = certificationSet.iterator().next();
                KPICounts recon = ReconciliationRepository.getStatusTotalsForCert(session,Long.parseLong(id));
                row.setTotal(recon.getOpen());
                row.setProposed(recon.getProposed());
                row.setCleared(recon.getCleared());
                row.setError(recon.getErrors());
            }
            else
            {
                if (!allocation.getTotal().isEmpty()) row.setTotal(countUniqueLines(allocation.getTotal()));
                if (!allocation.getProposed().isEmpty()) row.setProposed(countUniqueLines(allocation.getProposed()));
                if (!allocation.getCleared().isEmpty()) row.setCleared(countUniqueLines(allocation.getCleared()));
                if (!allocation.getErrors().isEmpty()) row.setError(countUniqueLines(allocation.getErrors()));
            }
            if (!allocation.getMissings().isEmpty()) missingChains.addAll(getUniqueLines(allocation.getMissings()));

            result.mergeConditionalResults(row);
            /*
            ReportingRecord existing = result.get(reportingKey);

            if(existing == null) {
                result.add(row);
            } else {
                out.println("Overriding current value for " + key);
                if(!"conditional".equalsIgnoreCase(key.type)) throw new Exception ("Incorrect merge!");
                existing.setCleared(row.getCleared());
                existing.setError(row.getError());
                existing.setSelected(row.getSelected());
            }

             */
            log.info("====================================================================================");
        }
        if(!missingChains.isEmpty()) createMissingsFile(missingChains,job);

        return result;
    }

    private void parseConditionalFile(List<String> files, ReportingAggregator result) throws Exception
    {
        for (String path : files)
        {
            String[] parts = path.split(":", -1);

            JobFile jf = session.getJobByJobId(Long.parseLong(parts[1])).getJobFileByName(parts[2]);

            try (BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(), ENCODING)))
            {
                br.readLine(); // header

                String line;

                while ((line = br.readLine()) != null)
                {
                    String[] cols = line.split(";", -1);

                    if(cols.length < 6)
                    {
                        log.error("Invalid conditional line: [" + line + "]");
                        continue;
                    }

                    if ("Period".equalsIgnoreCase(cols[0]))
                    {
                        log.error("Additional header conditional line: [" + line + "]");
                        continue;
                    }

                    FileKey key = new FileKey(
                            cols[0], // period
                            cols[1], // company code
                            cols[5], // account group
                            cols[2].toLowerCase(), // type
                            cols[5]  // name
                    );

                    log.info("parseConditionalFile.key: " + key);
                    ReportingRecord row = new ReportingRecord(key);

                    row.setTotal(Integer.parseInt(cols[3]));
                    row.setProposed(Integer.parseInt(cols[4]));

                    result.mergeConditionalSummary(row);
                    /*
                    out.println("ROW: " + ReportingCsvWriter.toCsv(row));
                    ReportingRecord existing = result.get(reportingKey);
                    out.println("EXISTING: " + (existing == null ? "NULL" : ReportingCsvWriter.toCsv(existing)));
                    if(existing == null) {
                        result.add(row);
                        out.println("ROW added...");
                    } else {
                        existing.setTotal(row.getTotal());
                        existing.setProposed(row.getProposed());
                        out.println("EXISTING updated...");
                    }

                     */
                }
            }
        }
    }

    private void createMissingsFile(Set<String> missingChains, Job job)
            throws Exception
    {
        String fileName = "missing_chains.csv";
        JobFile jf = JobFileService.createJobFile(session, job, fileName);
        try (FileOutputStream fos = new FileOutputStream(jf.getFileName()))
        {
            for(String chain:missingChains)
            {
                ResultFileWriter.write(fos, chain);
            }
        }
    }
    public static final String ENCODING = "UTF-8";

    private Set<String> getUniqueLines(List<String> paths)
            throws Exception
    {
        Set<String> collected = new HashSet<>();
        for (String path : paths)
        {
            String[] parts = path.split(":", -1);
            JobFile jf = session.getJobByJobId(Long.parseLong(parts[1])).getJobFileByName(parts[2]);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(), ENCODING)))
            {
                String line;
                while ((line = br.readLine()) != null)
                {
                    collected.add(line);
                }
            }
        }
        return collected;
    }
    private int countUniqueLines(List<String> paths)
            throws Exception
    {
        return getUniqueLines(paths).size();
    }

    FileKey getKey(String name)
    {
        if("missing_chains.csv".equals(name)) return specialKey("missing");
        if ("conditional_summary.csv".equals(name)) return specialKey("conditional");
        if ("file_statistics.csv".equals(name)) return specialKey("statistics");
        return FileKeyCodec.parseFileName(name);
    }

    private FileKey specialKey(String value)
    {
        return new FileKey(value, value, value, value, value);
    }
}
