package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.repository;

import com.redwood.scheduler.api.model.Table;
import com.redwood.scheduler.api.model.TableValue;
import com.redwood.scheduler.custom.kpi.kpi3.model.KPICounts;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingKey;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;

import java.util.Iterator;


public class ReportingMapper {

    public static ReportingRecord toRecord(Iterator<TableValue> rowValues) {

        if (!rowValues.hasNext()) return null;

        ReportingKey key = null;
        KPICounts counts = new KPICounts();
        boolean first = true;
        while (rowValues.hasNext()) {

            TableValue column = rowValues.next();
            if (first) {
                key = ReportingKey.parse(column.getKey());
                first = false;
            }

            switch (column.getColumnName()) {
                case ("open"): {
                    counts.setOpen(Integer.parseInt(column.getColumnValue()));
                    break;
                }
                case ("proposed"): {
                    counts.setProposed(Integer.parseInt(column.getColumnValue()));
                    break;
                }
                case ("cleared"): {
                    counts.setCleared(Integer.parseInt(column.getColumnValue()));
                    break;
                }
                case ("errors"): {
                    counts.setErrors(Integer.parseInt(column.getColumnValue()));
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown column: " + column.getColumnName());

            }

        }

        ReportingRecord record = new ReportingRecord(key);
        record.setKPICounts(counts);

        return record;
    }

    public static void toTableValues(Table repository, ReportingRecord record) {
        String key = record.getReportingKey().toString();
        upsert(repository, key, "open", String.valueOf(record.getCounts().getOpen()));
        upsert(repository, key, "proposed", String.valueOf(record.getCounts().getProposed()));
        upsert(repository, key, "cleared", String.valueOf(record.getCounts().getCleared()));
        upsert(repository, key, "errors", String.valueOf(record.getCounts().getErrors()));

    }

    private static void upsert(Table table, String key, String column, String value) {

        if (value == null) return;

        TableValue tv = table.getTableValueBySearchKeySearchColumnName(key, column);
        if (tv == null) {
            tv = table.createTableValue();
            tv.setKey(key);
            tv.setColumnName(column);
        }
        tv.setColumnValue(value);
    }

}
