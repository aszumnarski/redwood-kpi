package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.model.FileKey;
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

import static com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec.parseFileName;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

public class VolumesCollector
{
    private final SchedulerSession session;
    private final Job job;
    private final PrintWriter out;
    private final PrintWriter err;

    public VolumesCollector(SchedulerSession session, Job job, PrintWriter out, PrintWriter err)
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
        String files = getParameter(job,"IN_FILES");
        if(files.length() < 10) return null;
        Map<FileKey,List<String>> filesMap = new HashMap<>();

        for(String file : files.split(";",-1))
        {
            //out.println("processing: " + file);
            String[] parts = file.split("=",-1);
            //out.println(parts[0]);
            FileKey fileKey = getKey(parts[0]);
            //out.println(fileKey);
            filesMap.computeIfAbsent(fileKey, k -> new ArrayList<>()).add(parts[1]);
        }

        Map<FileKey,Row> rows = collectRows(filesMap);
        if (rows.isEmpty()) return null;

        String fileName = getParameter(job,"IN_FILENAME");
        JobFile jf = createJobFile(session, job, fileName);
        try (FileOutputStream fos = new FileOutputStream(jf.getFileName()))
        {
            write(fos,getHeader());
            for(FileKey key:rows.keySet())
            {
                Row row = rows.get(key);
                write(fos, row.toCsv());
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
        Set<String> certIds = new HashSet<>();
        for (FileKey key : filesMap.keySet())
        {
            List<String> total = new ArrayList<>();
            List<String> proposed = new ArrayList<>();
            List<String> selected = new ArrayList<>();
            List<String> cleared = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            List<String> missings = new ArrayList<>();
            List<String> certId = new ArrayList<>();
            for (String path : filesMap.get(key))
            {
                if (path.endsWith("total.csv")) total.add(path);
                else if (path.endsWith("proposed.csv")) proposed.add(path);
                else if (path.endsWith("selected.csv")) selected.add(path);
                else if (path.endsWith("cleared.csv")) cleared.add(path);
                else if (path.endsWith("errors.csv")) errors.add(path);
                else if (path.endsWith("missing_chains.csv")) missings.add(path);
                else if (path.endsWith("certId.csv")) certId.add(path);
                else out.println("file name error: " + path);
            }

            boolean hasAnyKpiFile = !(total.isEmpty() && proposed.isEmpty() && selected.isEmpty() && cleared.isEmpty() && errors.isEmpty() && certId.isEmpty());
            if (!hasAnyKpiFile)
            {
                if (!missings.isEmpty()) missingChains.addAll(getUniqueLines(missings));
                continue;
            }

            Row row = new Row(key);

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
                if (!missings.isEmpty()) missingChains.addAll(getUniqueLines(missings));
            }
            else
            {
                if (!total.isEmpty()) row.total = countUniqueLines(total);
                if (!proposed.isEmpty()) row.proposed = countUniqueLines(proposed);
                if (!selected.isEmpty()) row.selected = countUniqueLines(selected);
                if (!cleared.isEmpty()) row.cleared = countUniqueLines(cleared);
                if (!errors.isEmpty()) row.error = countUniqueLines(errors);
                if (!missings.isEmpty()) missingChains.addAll(getUniqueLines(missings));
            }
            result.put(key, row);
        }
        if(!missingChains.isEmpty()) createMissingsFile(missingChains);
        //if(!certIds.isEmpty())
        return result;
    }
    private void createMissingsFile(Set<String> missingChains)
            throws Exception
    {
        String fileName = "missing_chains.csv";
        JobFile jf = createJobFile(session, job, fileName);
        try (FileOutputStream fos = new FileOutputStream(jf.getFileName()))
        {
            for(String chain:missingChains)
            {
                write(fos, chain);
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

        String toCsv()
        {
            return COLUMNS.stream().map(c -> c.extractor.apply(this)).collect(Collectors.joining(";"));
        }

    }

    FileKey getKey(String name)
            throws Exception
    {
        String periods = getParameter(job,"IN_PERIODS");
        int periodInt = Integer.parseInt(periods);
        int idx = name.indexOf("_");
        if(idx == -1) throw new Exception("Illegal name!");
        if(periodInt == 1) idx = -1;
        String nameNoIndex = name.substring(idx + 1);
        if("missing_chains.csv".equals(nameNoIndex)) return new FileKey("missing", "missing", "missing", "missing", "missing");
        return parseFileName(nameNoIndex);
    }
}