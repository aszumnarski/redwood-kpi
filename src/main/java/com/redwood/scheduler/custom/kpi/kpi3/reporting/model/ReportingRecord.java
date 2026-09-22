package com.redwood.scheduler.custom.kpi.kpi3.reporting.model;

import com.redwood.scheduler.custom.kpi.kpi3.file.FileKey;

public class ReportingRecord {

    private final ReportingKey reportingKey;
    private final String name;
    private int total;
    private int proposed;
    private int selected;
    private int cleared;
    private int error;

    public ReportingRecord(FileKey key) {
        this.reportingKey = ReportingKey.from(key);
        this.name = key.name();
    }

    public boolean isEmpty() {
        return total == 0 && proposed == 0 && cleared == 0 && error == 0;
    }

    public ReportingKey getReportingKey(){
        return reportingKey;
    }

    public String getPeriod() {
        return reportingKey.getPeriod();
    }

    public String getCompanyCode() {
        return reportingKey.getCompanyCode();
    }

    public String getType() {
        return reportingKey.getType();
    }

    public String getAccountGroup() {
        return reportingKey.getAccountGroup();
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

    public String getName() {
        return name;
    }

    @Override
    public String toString()
    {
        return reportingKey + ";"
                + total + ";"
                + proposed + ";"
                + cleared + ";"
                + error;
    }

}
