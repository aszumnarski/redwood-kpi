package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.aggregation;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileKey;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec;
import com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.model.KPICounts;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.csv.ReportingCsvWriter;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileAllocation;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.repository.ReconciliationRepository;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class VolumeAggregator
{
    private final SchedulerSession session;
    private final Job job;
    private final PrintWriter out;
    private final PrintWriter err;

    public VolumeAggregator(SchedulerSession session, Job job, PrintWriter out, PrintWriter err)
    {
        this.session = session;
        this.job = job;
        this.out = out;
        this.err = err;
    }

    public JobFile collect()
            throws Exception
    {
        String files = JobParameterHelper.getParameter(job,"IN_FILES");
        if(files.length() < 10) return null;
        Map<FileKey,List<String>> filesMap = new HashMap<>();

        for(String file : files.split(";",-1))
        {
            out.println("processing: " + file);
            String[] parts = file.split("=",-1);
            out.println("will produce file key from: " + parts[0]);
            FileKey fileKey = getKey(parts[0]);
            out.println("File key is:" + fileKey);
            filesMap.computeIfAbsent(fileKey, k -> new ArrayList<>()).add(parts[1]);
        }

        ReportingAggregator aggregator = collectRows(filesMap);

        if (aggregator.getRecords().isEmpty()) return null;


        String fileName = JobParameterHelper.getParameter(job,"IN_FILENAME");
        JobFile jf = JobFileService.createJobFile(session, job, fileName);
        try (FileOutputStream fos = new FileOutputStream(jf.getFileName()))
        {
            JobFileService.write(fos, ReportingCsvWriter.getHeader());
            for(ReportingRecord row:aggregator.getRecords())
            {
                if(row.isEmpty()) continue;
                JobFileService.write(fos, ReportingCsvWriter.toCsv(row));
            }
        }
        return jf;
    }




    private ReportingAggregator collectRows(Map<FileKey, List<String>> filesMap)
            throws Exception
    {
        ReportingAggregator result = new ReportingAggregator();
        Set<String> missingChains = new HashSet<>();
        Set<String> processedConditionalFiles = new HashSet<>();


        for (FileKey key : filesMap.keySet())
        {
            out.println("collectRows looping key: " + key);

            FileAllocation allocation = new FileAllocation();

            for (String path : filesMap.get(key))
            {
                out.println("Allocating path: " + path);
                allocation.add(path);
            }

            if (!allocation.hasAnyKpiFile())
            {
                if (!allocation.getMissings().isEmpty())
                {
                    missingChains.addAll(
                            getUniqueLines(
                                    allocation.getMissings()));
                }

                continue;
            }

            out.println("conditional.isEmpty()=" + allocation.getConditional().isEmpty());
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
                out.println("====================================================================================");
                continue;
            }

            ReportingRecord row = new ReportingRecord(key);

            out.println("certId.isEmpty()=" +  allocation.getCertId().isEmpty());
            if (!allocation.getCertId().isEmpty())
            {
                Set<String> certificationSet = getUniqueLines(allocation.getCertId());
                if(certificationSet.size() != 1)
                {
                    err.println("More than 1 certification found! " + certificationSet + " for key " + key);
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
            out.println("====================================================================================");
        }
        if(!missingChains.isEmpty()) createMissingsFile(missingChains);

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
                        err.println("Invalid conditional line: [" + line + "]");
                        continue;
                    }

                    if ("Period".equalsIgnoreCase(cols[0]))
                    {
                        err.println("Additional header conditional line: [" + line + "]");
                        continue;
                    }

                    FileKey key = new FileKey(
                            cols[0], // period
                            cols[1], // company code
                            cols[5], // account group
                            cols[2].toLowerCase(), // type
                            cols[5]  // name
                    );

                    out.println("parseConditionalFile.key: " + key);
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

    private void createMissingsFile(Set<String> missingChains)
            throws Exception
    {
        String fileName = "missing_chains.csv";
        JobFile jf = JobFileService.createJobFile(session, job, fileName);
        try (FileOutputStream fos = new FileOutputStream(jf.getFileName()))
        {
            for(String chain:missingChains)
            {
                JobFileService.write(fos, chain);
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
            throws Exception
    {
        if("missing_chains.csv".equals(name)) return specialKey("missing");
        if ("conditional_summary.csv".equals(name)) return specialKey("conditional");
        if ("file_statistics.csv".equals(name)) return specialKey("statistics");

        String periods = JobParameterHelper.getParameter(job,"IN_PERIODS");
        boolean singlePeriod = "1".equals(periods);
        int idx = name.indexOf("_");
        if(idx == -1) throw new Exception("Illegal name! " + name + " names have to include '_' .");
        if(singlePeriod) idx = -1;
        String nameNoIndex = name.substring(idx + 1);
        return FileKeyCodec.parseFileName(nameNoIndex);
    }

    private FileKey specialKey(String value)
    {
        return new FileKey(value, value, value, value, value);
    }
}