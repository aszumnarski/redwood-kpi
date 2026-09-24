package com.redwood.scheduler.custom.kpi.kpi3.monitoring;

import com.redwood.scheduler.custom.kpi.kpi3.model.ClearingResult;
import com.redwood.scheduler.custom.kpi.kpi3.model.KPICounts;

public class CollectorStats
{

    private KPICounts counts = new KPICounts();

    public int getOpen() {
        return counts.getOpen();
    }
    public int getProposed() { return counts.getProposed(); }

    public void apply(ClearingResult result)
    {
        counts.setProposed(counts.getProposed() + result.proposed());
        counts.setCleared(counts.getCleared() + result.itemsCleared());
        counts.setErrors(counts.getErrors() + result.errors());
    }

    public void add(CollectorStats other)
    {
        counts.add(other.getCounts());

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

    public void setProposed(int proposed) {
        counts.setProposed(proposed);
    }
}