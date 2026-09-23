package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model;

import com.redwood.scheduler.custom.kpi.kpi3.file.FileKey;

import java.util.Objects;

public class ReportingKey
{
    private final String period;
    private final String companyCode;
    private final String accountGroup;
    private final String type;

    public ReportingKey(String period, String companyCode, String accountGroup, String type)
    {
        this.period = period;
        this.companyCode = companyCode;
        this.accountGroup = accountGroup;
        this.type = type;
    }

    public String getPeriod()
    {
        return period;
    }

    public String getCompanyCode()
    {
        return companyCode;
    }

    public String getAccountGroup()
    {
        return accountGroup;
    }

    public String getType()
    {
        return type;
    }

    public static ReportingKey from(FileKey key)
    {
        return new ReportingKey(key.period(), key.companyCode(), key.accountGroup(), key.type());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ReportingKey that = (ReportingKey) o;

        return Objects.equals(period, that.period)
                && Objects.equals(companyCode, that.companyCode)
                && Objects.equals(accountGroup, that.accountGroup)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(period, companyCode, accountGroup, type);
    }

    @Override
    public String toString()
    {
        return period + ";" + companyCode + ";" + type + ";" + accountGroup;
    }
}