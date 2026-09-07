package com.redwood.scheduler.custom.kpi.kpi3.model;

public class CollectorStats
{
    private Long clearId = -1L;

    private int totalOpenItems;
    private int itemsCleared;
    private int ruleSet1;
    private int autoClear;
    private int errors;
    private int suggestedClear;
    private int totalCollected;

    public int getRuleSet1() {
        return ruleSet1;
    }

    public int getAutoClear() {
        return autoClear;
    }

    public int getSuggestedClear() {
        return suggestedClear;
    }

    public int getItemsCleared() {
        return itemsCleared;
    }

    public int getTotalOpenItems() {
        return totalOpenItems;
    }

    public int getTotalCollected() {
        return totalCollected;
    }

    public int getErrors() {
        return errors;
    }

    void addDt(DataTransformer dt)
    {
        if (ruleSet1 == 0) ruleSet1 = dt.getRuleSet1();
        autoClear += dt.getAutoClear();
        suggestedClear += dt.getSuggestedClear();
        totalCollected = autoClear + suggestedClear;
    }

    void apply(ClearingResult result)
    {
        clearId = result.clearId();
        itemsCleared += result.itemsCleared();
        errors += result.errors();
    }

    public void setTotalOpenItems(int totalOpenItems) {
        this.totalOpenItems = totalOpenItems;
    }

    public void setTotalCollectedItems(int totalCollected) {
        this.totalCollected = totalCollected;
    }

    public Long getClearId() {
        return clearId;
    }
}