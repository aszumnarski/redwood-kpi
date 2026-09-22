package com.redwood.scheduler.custom.kpi.kpi3.reporting.model;

import com.redwood.scheduler.custom.kpi.kpi3.file.FileKey;

import java.util.stream.Collectors;

import com.redwood.scheduler.custom.kpi.kpi3.service.VolumeAggregator;

public class ReportingRecord {

    private final String period;
    private final String companyCode;
    private final String type;
    private final String accountGroup;
    private final String name;
    private int total;
    private int proposed;
    private int selected;
    private int cleared;
    private int error;

    public ReportingRecord(FileKey key) {
        this.period = key.period();
        this.companyCode = key.companyCode();
        this.type = key.type();
        this.accountGroup = key.accountGroup();
        this.name = key.name();
    }

    public boolean isEmpty() {
        return total == 0 && proposed == 0 && cleared == 0 && error == 0;
    }

    public String toCsv() {
        return VolumeAggregator.COLUMNS.stream().map(c -> c.extractor.apply(this)).collect(Collectors.joining(";"));
    }

    public String getPeriod() {
        return period;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public String getType() {
        return type;
    }

    public String getAccountGroup() {
        return accountGroup;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public int getCleared() {
        return cleared;
    }

    public void setCleared(int cleared) {
        this.cleared = cleared;
    }

    public int getProposed() {
        return proposed;
    }

    public void setProposed(int proposed) {
        this.proposed = proposed;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }


}
