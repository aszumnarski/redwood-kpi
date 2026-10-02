package com.redwood.scheduler.custom.kpi.kpi3.model;

public class KPICounts
{
    private int open;
    private int proposed;
    private int cleared;
    private int errors;


    public void addOpen(int value)
    {
        open += value;
    }

    public void addProposed(int value)
    {
        proposed += value;
    }

    public void addCleared(int value)
    {
        cleared += value;
    }

    public void addErrors(int value)
    {
        errors += value;
    }

    public void add(KPICounts other)
    {
        open += other.open;
        proposed += other.proposed;
        cleared += other.cleared;
        errors += other.errors;
    }

    public boolean isEmpty() {
        return open == 0 && proposed == 0 && cleared == 0 && errors == 0;
    }

    public void clear()
    {
        open = 0;
        proposed = 0;
        cleared = 0;
        errors = 0;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getProposed() {
        return proposed;
    }

    public void setProposed(int proposed) {
        this.proposed = proposed;
    }

    public int getCleared() {
        return cleared;
    }

    public void setCleared(int cleared) {
        this.cleared = cleared;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    @Override
    public String toString()
    {
        return "KPICounts{" +
                "open=" + open +
                ", proposed=" + proposed +
                ", cleared=" + cleared +
                ", errors=" + errors +
                '}';
    }

    public String toCsv(){
        return open + ";" + proposed + ";" + cleared + ";" + errors;
    }

}
