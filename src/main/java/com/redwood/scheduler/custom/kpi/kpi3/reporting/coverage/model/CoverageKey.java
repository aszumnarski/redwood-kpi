package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconConfiguration;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;

import java.util.Objects;

public class CoverageKey {

    private String companyCode;
    private String accountItem;

    public CoverageKey() {
    }

    public CoverageKey(
            String companyCode,
            String accountItem) {

        this.companyCode = companyCode;
        this.accountItem = accountItem;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getAccountItem() {
        return accountItem;
    }

    public void setAccountItem(String accountItem) {
        this.accountItem = accountItem;
    }

    public String getKey() {
        return companyCode + ":" + accountItem;
    }

    public static CoverageKey from(ReconConfiguration config) {

        return new CoverageKey(
                config.getCompanyCode(),
                config.getAccountItem());
    }

    public static CoverageKey from(JobReportingRecord record) {

        return new CoverageKey(
                record.getCompanyCode(),
                record.getAccountItem());
    }

    public static CoverageKey from(ScheduledCoverageEntry entry) {
        return new CoverageKey(
                entry.key().getCompanyCode(),
                entry.key().getAccountItem());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CoverageKey)) {
            return false;
        }

        CoverageKey that = (CoverageKey) o;

        return Objects.equals(companyCode, that.companyCode)
                && Objects.equals(accountItem, that.accountItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                companyCode,
                accountItem);
    }

    @Override
    public String toString() {
        return getKey();
    }
}
