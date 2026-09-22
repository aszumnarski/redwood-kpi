package com.redwood.scheduler.custom.kpi.kpi3.reporting.csv;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.model.ReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.service.VolumeAggregator;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReportingCsvWriter {

    public static String getHeader()
    {
        return COLUMNS.stream().map(c -> c.header).collect(Collectors.joining(";"));
    }

    public static String toCsv(ReportingRecord record) {
        return COLUMNS.stream().map(c -> c.extractor.apply(record)).collect(Collectors.joining(";"));
    }

    public static class Column
    {
        final String header;
        public final Function<ReportingRecord, String> extractor;
        public Column(String header, Function<ReportingRecord, String> extractor)
        {
            this.header = header;
            this.extractor = extractor;
        }
    }

    public static final List<Column> COLUMNS = java.util.Arrays.asList(
            new Column("Period",           r -> nvl(r.getPeriod())),
            new Column("Company Code",     r -> nvl(r.getCompanyCode())),
            new Column("Type",             r -> nvl(r.getType())),
            new Column("Total Open Item",  r -> String.valueOf(r.getTotal())),
            new Column("Proposed",         r -> String.valueOf(r.getProposed())),
            new Column("Cleared",          r -> String.valueOf(r.getCleared())),
            new Column("Error",            r -> String.valueOf(r.getError())),
            new Column("Account Item",     r -> nvl(r.getAccountGroup()))
    );

    private static String nvl(String s) { return s == null ? "" : s; }

}
