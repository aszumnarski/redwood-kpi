package com.redwood.scheduler.custom.kpi.kpi3.date;

import com.redwood.scheduler.api.date.DateTimeZone;

public final class PeriodUtil
{
    private PeriodUtil() {}

    public static String getPeriod(DateTimeZone runStartDate)
    {
        if (runStartDate == null) return "";

        DateTimeZone shifted = runStartDate.expression("add 3 months");

        return "FY"
                + shifted.toFormattedString("yyyy")
                + "P"
                + shifted.toFormattedString("MM");
    }
}
