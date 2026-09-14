package com.redwood.scheduler.custom.kpi.kpi3.monitoring;

import com.redwood.scheduler.api.date.DateTimeZone;

import java.io.PrintWriter;

public class Stopwatch
{
    private long startMs;

    public Stopwatch()
    {
        reset();
    }

    public void reset()
    {
        startMs = DateTimeZone.now().getUTCMilliSecs();
    }

    public long elapsedMs()
    {
        return DateTimeZone.now().getUTCMilliSecs() - startMs;
    }

    public long elapsedSec()
    {
        return elapsedMs() / 1000;
    }

    public void log(PrintWriter out, String message)
    {
        out.println(message + " in " + elapsedSec() + " sec");
    }
}
