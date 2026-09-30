package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model;

import com.redwood.scheduler.custom.kpi.kpi3.file.FileKey;
import com.redwood.scheduler.custom.kpi.kpi3.model.KPICounts;

public class ReportingRecord {

    private final ReportingKey reportingKey;
    private final KPICounts counts = new KPICounts();

    public ReportingRecord(FileKey key) {
        this.reportingKey = ReportingKey.from(key);
    }

    public ReportingRecord(ReportingKey reportingKey){
        this.reportingKey = reportingKey;
    }

    public void addKPICounts(KPICounts other){
        counts.add(other);
    }

    public boolean isEmpty() { return counts.isEmpty(); }

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
        return counts.getErrors();
    }

    public void setError(int errors) { counts.setErrors(errors); }

    public int getCleared() {
        return counts.getCleared();
    }

    public void setCleared(int cleared) { counts.setCleared(cleared); }

    public int getProposed() {
        return counts.getProposed();
    }

    public void setProposed(int proposed) { counts.setProposed(proposed); }

    public int getTotal() { return counts.getOpen(); }

    public void setTotal(int total) { counts.setOpen(total); }

    @Override
    public String toString()
    {
        return reportingKey + ";"
                + counts.getOpen() + ";"
                + counts.getProposed() + ";"
                + counts.getCleared() + ";"
                + counts.getErrors();
    }

}
