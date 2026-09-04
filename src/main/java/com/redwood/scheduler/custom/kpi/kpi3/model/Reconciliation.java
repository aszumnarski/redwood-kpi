package com.redwood.scheduler.custom.kpi.kpi3.model;

public class Reconciliation
{
    private int total = 0;
    private int proposed = 0;
    private int selected = 0;
    private int cleared = 0;
    private int errors = 0;

    public Integer getTotal()
    {
        return total;
    }
    public Integer getProposed()
    {
        return proposed;
    }
    public Integer getSelected()
    {
        return selected;
    }
    public Integer getCleared()
    {
        return cleared;
    }
    public Integer getErrors()
    {
        return errors;
    }
    public void addTotal(int total)
    {
        this.total += total;
    }
    public void addProposed(int proposed)
    {
        this.proposed += proposed;
    }
    public void addSelected(int selected)
    {
        this.selected += selected;
    }
    public void addCleared(int cleared)
    {
        this.cleared += cleared;
    }
    public void addErrors(int errors)
    {
        this.errors += errors;
    }
    public void clear()
    {
        total = 0;
        proposed = 0;
        selected = 0;
        cleared = 0;
        errors = 0;
    }
}