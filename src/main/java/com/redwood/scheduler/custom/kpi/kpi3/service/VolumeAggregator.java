package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileKey;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec;
import com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.model.Reconciliation;
import com.redwood.scheduler.custom.kpi.kpi3.repository.ReconciliationRepository;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;
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


    static class Column
    {
        final String header;
        final Function<Row, String> extractor;
        Column(String header, Function<Row, String> extractor)
        {
            this.header = header;
            this.extractor = extractor;
        }
    }

    private static final List<Column> COLUMNS = java.util.Arrays.asList(
            new Column("Period",           r -> nvl(r.period)),
            new Column("Company Code",     r -> nvl(r.companyCode)),
            new Column("Type",             r -> nvl(r.type)),
            new Column("Total Open Item",  r -> String.valueOf(r.total)),
            new Column("Proposed",         r -> String.valueOf(r.proposed)),
            new Column("Cleared",          r -> String.valueOf(r.cleared)),
            new Column("Error",            r -> String.valueOf(r.error)),
            new Column("Account Item",     r -> nvl(r.accountGroup))
    );

    private static String nvl(String s) { return s == null ? "" : s; }

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

        Map<FileKey,Row> rows = collectRows(filesMap);
        if (rows.isEmpty()) return null;

        String fileName = JobParameterHelper.getParameter(job,"IN_FILENAME");
        JobFile jf = JobFileService.createJobFile(session, job, fileName);
        try (FileOutputStream fos = new FileOutputStream(jf.getFileName()))
        {
            JobFileService.write(fos,getHeader());
            for(FileKey key:rows.keySet())
            {
                Row row = rows.get(key);
                if("B013100_TradeAR3rdParty_CFAVF".equals(row.accountGroup))
                {
                    out.println("WRITING: " + row.toCsv());
                }
                if(row.isEmpty()) continue;
                JobFileService.write(fos, row.toCsv());
            }
        }
        return jf;
    }


    String getHeader()
    {
        return COLUMNS.stream().map(c -> c.header).collect(Collectors.joining(";"));
    }

    private Map<FileKey, Row> collectRows(Map<FileKey, List<String>> filesMap)
            throws Exception
    {
        Map<FileKey, Row> result = new HashMap<>();
        Set<String> missingChains = new HashSet<>();
        Set<String> processedConditionalFiles = new HashSet<>();

        for (FileKey key : filesMap.keySet())
        {
            out.println("collectRows looping key: " + key);
            List<String> conditional = new ArrayList<>();
            List<String> total = new ArrayList<>();
            List<String> proposed = new ArrayList<>();
            List<String> selected = new ArrayList<>();
            List<String> cleared = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            List<String> missings = new ArrayList<>();
            List<String> certId = new ArrayList<>();
            for (String path : filesMap.get(key))
            {
                out.println("Allocating path: " + path);
                if (path.endsWith("total.csv")) total.add(path);
                else if (path.endsWith("proposed.csv")) proposed.add(path);
                else if (path.endsWith("selected.csv")) selected.add(path);
                else if (path.endsWith("cleared.csv")) cleared.add(path);
                else if (path.endsWith("errors.csv")) errors.add(path);
                else if (path.endsWith("missing_chains.csv")) missings.add(path);
                else if (path.endsWith("certId.csv")) certId.add(path);
                else if (path.endsWith("conditional_summary.csv")) conditional.add(path);
                else out.println("file name error: " + path);
            }

            boolean hasAnyKpiFile = !(conditional.isEmpty() && total.isEmpty() && proposed.isEmpty() && selected.isEmpty() && cleared.isEmpty() && errors.isEmpty() && certId.isEmpty());
            out.println("hasAnyKpiFile=" + hasAnyKpiFile);
            if (!hasAnyKpiFile)
            {
                if (!missings.isEmpty()) missingChains.addAll(getUniqueLines(missings));
                continue;
            }
            out.println("conditional.isEmpty()=" + conditional.isEmpty());
            if (!conditional.isEmpty())
            {
                String conditionalFile = conditional.get(0);

                if (processedConditionalFiles.add(conditionalFile))
                {
                    parseConditionalFile(conditional, result);
                }

                if (!missings.isEmpty())
                {
                    missingChains.addAll(getUniqueLines(missings));
                }
                out.println("====================================================================================");
                continue;
            }

            Row row = new Row(key);

            out.println("certId.isEmpty()=" + certId.isEmpty());
            if (!certId.isEmpty())
            {
                Set<String> certificationSet = getUniqueLines(certId);
                if(certificationSet.size() != 1)
                {
                    err.println("More than 1 certification found! " + certificationSet + " for key " + key);
                    //throw new Exception ("More than 1 certification found! " + certificationSet + " for key " + key);
                }
                String id = certificationSet.iterator().next();
                Reconciliation recon = ReconciliationRepository.getStatusTotalsForCert(session,Long.parseLong(id));
                row.total = recon.getTotal();
                row.proposed = recon.getProposed();
                row.selected = recon.getSelected();
                row.cleared = recon.getCleared();
                row.error = recon.getErrors();
            }
            else
            {
                if (!total.isEmpty()) row.total = countUniqueLines(total);
                if (!proposed.isEmpty()) row.proposed = countUniqueLines(proposed);
                if (!selected.isEmpty()) row.selected = countUniqueLines(selected);
                if (!cleared.isEmpty()) row.cleared = countUniqueLines(cleared);
                if (!errors.isEmpty()) row.error = countUniqueLines(errors);
            }
            if (!missings.isEmpty()) missingChains.addAll(getUniqueLines(missings));

            Row existing = result.get(key);

            if(existing == null) {
                result.put(key, row);
            } else {
                out.println("Overriding current value for " + key);
                if(!"conditional".equalsIgnoreCase(key.type)) throw new Exception ("Incorrect merge!");
                existing.cleared = row.cleared;
                existing.error = row.error;
                existing.selected = row.selected;
            }
            out.println("====================================================================================");
        }
        if(!missingChains.isEmpty()) createMissingsFile(missingChains);
        //if(!certIds.isEmpty())

        result.forEach((k,v) -> {
            if("B013100_TradeAR3rdParty_CFAVF".equals(v.accountGroup))
            {
                out.println("END COLLECTROWS: " + v.toCsv());
            }
        });

        return result;
    }

    private void parseConditionalFile(List<String> files, Map<FileKey, Row> result) throws Exception
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
                    Row row = new Row(key);

                    row.total = Integer.parseInt(cols[3]);
                    row.proposed = Integer.parseInt(cols[4]);

                    out.println("ROW: " + row.toCsv());
                    Row existing = result.get(key);
                    out.println("EXISTING: " + (existing == null ? "NULL" : existing.toCsv()));
                    if(existing == null) {
                        result.put(key, row);
                        out.println("ROW added...");
                    } else {
                        existing.total = row.total;
                        existing.proposed = row.proposed;
                        out.println("EXISTING updated...");
                    }
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

    class Row
    {
        final String period;
        final String companyCode;
        final String type;
        final String accountGroup;
        final String name;
        int total;
        int proposed;
        int selected;
        int cleared;
        int error;

        Row(FileKey key)
        {
            this.period = key.period();
            this.companyCode = key.companyCode();
            this.type = key.type();
            this.accountGroup = key.accountGroup();
            this.name = key.name();
        }

        boolean isEmpty()
        {
            return total == 0
                    && proposed == 0
                    && cleared == 0
                    && error == 0;
        }

        String toCsv()
        {
            return COLUMNS.stream().map(c -> c.extractor.apply(this)).collect(Collectors.joining(";"));
        }

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