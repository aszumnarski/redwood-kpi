package com.redwood.scheduler.custom.kpi.kpi3.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileStatistics
{
    private final Map<String, Long> rowsByFile = new HashMap<>();
    private final Map<String, Integer> writesByFile = new HashMap<>();

    private long lookups;
    private long hits;
    private long misses;

    public void lookup()
    {
        lookups++;
    }

    public void hit()
    {
        hits++;
    }

    public void miss()
    {
        misses++;
    }

    public long getLookups()
    {
        return lookups;
    }

    public long getHits()
    {
        return hits;
    }

    public long getMisses()
    {
        return misses;
    }

    public void addRows(String fileName, long rows)
    {
        rowsByFile.merge(fileName, rows, Long::sum);
        writesByFile.merge(fileName, 1, Integer::sum);
    }

    public long getTotalRows()
    {
        return rowsByFile.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    public List<String> toCsvRows()
    {
        List<String> rows = new ArrayList<>();

        rows.add("Metric,Value");
        rows.add("TotalRows," + getTotalRows());
        rows.add("TotalFiles," + rowsByFile.size());
        rows.add("Lookups," + lookups);
        rows.add("Hits," + hits);
        rows.add("Misses," + misses);
        rows.add("");

        rows.add("FileName,Rows,Writes");

        rowsByFile.entrySet()
                .stream()
                .sorted((a, b) ->
                        Long.compare(b.getValue(), a.getValue()))
                .forEach(e ->
                        rows.add(
                                e.getKey() + "," +
                                        e.getValue() + "," +
                                        writesByFile.get(e.getKey())
                        ));

        return rows;
    }
}

