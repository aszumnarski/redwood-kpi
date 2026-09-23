package com.redwood.scheduler.custom.kpi.kpi3.monitoring;

import com.redwood.scheduler.custom.kpi.kpi3.model.ClearingResult;
import com.redwood.scheduler.custom.kpi.kpi3.model.KPICounts;

public class CollectorStats
{

    private KPICounts counts = new KPICounts();

    public int getCleared() {
        return counts.getCleared();
    }
    public int getOpen() {
        return counts.getOpen();
    }
    public int getErrors() {
        return counts.getErrors();
    }
    public int getProposed() { return counts.getProposed(); }

    public void addDt(CollectorStats other)
    {
        if (counts.getOpen() == 0) counts.setOpen(other.getOpen());
        counts.setProposed(counts.getProposed() + other.getProposed());
        counts.setCleared(counts.getCleared() + other.getCleared());
        counts.setErrors(counts.getErrors() + other.getErrors());
    }

    public void apply(ClearingResult result)
    {
        counts.setProposed(counts.getProposed() + result.proposed());
        counts.setCleared(counts.getCleared() + result.itemsCleared());
        counts.setErrors(counts.getErrors() + result.errors());
    }

    public void add(CollectorStats other)
    {
        counts.setOpen(counts.getOpen() + other.getOpen());
        counts.setCleared(counts.getCleared()+ other.getCleared());
        counts.setErrors(counts.getErrors() + other.getErrors());
    }

    public KPICounts getCounts()
    {
        return counts;
    }

    public void setCounts(KPICounts counts)
    {
        this.counts = counts;
    }

    public void setOpen(int open) { counts.setOpen(open); }


    public void addProposed(int proposed) {
        counts.setProposed(counts.getProposed() + proposed);
    }

    public void setProposed(int proposed) {
        counts.setProposed(proposed);
    }
}