package com.redwood.scheduler.custom.kpi.kpi3;
import com.redwood.scheduler.api.date.DateTimeZone;
import java.util.Objects;

class FileKey
{
    public String period;
    public String companyCode;
    public String accountGroup;
    public String type;
    public String name;

    public FileKey(String period,String companyCode,String accountGroup,String type,String name)
    {
        this.period = period;
        this.companyCode = companyCode;
        this.accountGroup = accountGroup;
        this.type = type;
        this.name = name;
    }

    public FileKey(DateTimeZone period,String companyCode,String accountGroup,String type,String name)
    {
        this.period = Util.getPeriod(period);
        this.companyCode = companyCode;
        this.accountGroup = accountGroup;
        this.type = type;
        this.name = name;
    }


    public String period() { return period; }
    public String companyCode() { return companyCode; }
    public String type() { return type; }
    public String accountGroup() { return accountGroup; }
    public String name() { return name; }


    @Override
    public int hashCode()
    {
        return Objects.hash(period, companyCode, type, accountGroup);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FileKey other = (FileKey) obj;
        return Objects.equals(period, other.period) && Objects.equals(companyCode, other.companyCode) &&
                Objects.equals(type, other.type) && Objects.equals(accountGroup, other.accountGroup);
    }

    @Override public String toString()
    {
        return period + ";" + companyCode + ";" + type + ";" + accountGroup + ";" + name;
    }

}