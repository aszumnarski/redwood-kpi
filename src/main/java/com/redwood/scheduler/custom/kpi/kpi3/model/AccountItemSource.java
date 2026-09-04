package com.redwood.scheduler.custom.kpi.kpi3.model;

public interface AccountItemSource
{
    AccountItemContext getContext();

    Long getClearId();
    Long getPrepId();

    int getTotalOpenItems();
    int getItemsCleared();

    int getRuleSet1();
    int getAutoClear();
    int getErrors();
    int getSuggestedClear();
    int getTotalCollected();
}

