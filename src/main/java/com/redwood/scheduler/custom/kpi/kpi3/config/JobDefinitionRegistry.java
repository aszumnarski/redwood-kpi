package com.redwood.scheduler.custom.kpi.kpi3.config;

public enum JobDefinitionRegistry
{
    CONDITIONAL_REVIEW(
            "CUS_FCA_TD_BSC_AccountItemLoop_CONDITIONAL_AUTOCLEAR_REVIEW",
            JobType.CONDITIONAL, HandlerType.CONDITIONAL_REVIEW),
    CONDITIONAL_REVIEW_WEA(
            "CUS_FCA_TD_BSC_AccountItemLoop_CONDITIONAL_AUTOCLEAR_REVIEW_WEA",
            JobType.CONDITIONAL, HandlerType.CONDITIONAL_REVIEW),
    CONDITIONAL_REQUEST(
            "CUS_SPD_BSC_CONDITIONAL_AUTOCLEAR_REQUEST",
            JobType.CONDITIONAL, HandlerType.CONDITIONAL_REQUEST),
    AUTOCLEAR_WEA(
            "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_WEA",
            JobType.AUTO, HandlerType.AUTO),
    AUTOCLEAR_SHERPAX_LDGRP(
            "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_SHERPAX_LDGRP",
            JobType.AUTO, HandlerType.AUTO),
    AUTOCLEAR_WEA_backupBSIS(
            "CUS_TD_BSC_AUTOCLEAR_WEA_backupBSIS",
            JobType.AUTO, HandlerType.AUTO),
    AUTOCLEAR_SHERPAX_new(
            "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_SHERPAX_new",
            JobType.AUTO, HandlerType.AUTO),
    AUTOCLEAR_CB_IN_byAccountItem(
            "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_byAccountItem",
            JobType.AUTO, HandlerType.AUTO),
    AUTOCLEAR_CB_IN_bySchedule(
            "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_bySchedule",
            JobType.AUTO, HandlerType.AUTO),
    AutoClear_AccountItemLoop_SHERPAX_CASH(
            "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH",
            JobType.AUTO, HandlerType.AUTO),
    AutoClear_AccountItemLoop_SHERPAX_CASH_SOFOM(
            "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_SOFOM",
            JobType.AUTO, HandlerType.AUTO),
    AutoClear_AccountItemLoop_SHERPAX_CASH_US(
            "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_US",
            JobType.AUTO, HandlerType.AUTO),
    OneSided_GRIR_SHERPAX(
            "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_GRIR_SHERPAX",
            JobType.RECON, HandlerType.RECON),
    OneSided_GRIR(
            "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_GRIR",
            JobType.RECON, HandlerType.RECON),
    OneSided_BR(
            "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_BR",
            JobType.RECON, HandlerType.RECON),
    OneSided_CB_SHERPAX(
            "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_CB_SHERPAX",
            JobType.RECON, HandlerType.RECON),
    OneSided_APAR_SHERPAX(
            "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_APAR_SHERPAX",
            JobType.RECON, HandlerType.RECON),
    TwoSided_SFS_LIQ(
            "CUS_FCA_TD_BSC_AccountItemLoop_TwoSided_SFS_LIQ",
            JobType.RECON, HandlerType.RECON),
    OneSided_CB_WEA(
            "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_CB_WEA",
            JobType.RECON, HandlerType.RECON),
    OneSided_APAR_WEA(
            "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_APAR_WEA",
            JobType.RECON, HandlerType.RECON),
    TwoSided_CB(
            "CUS_FCA_TD_BSC_AccountItemLoop_TwoSided_CB",
            JobType.RECON, HandlerType.RECON),
    OneSided_CB(
            "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_CB",
            JobType.RECON, HandlerType.RECON),
    OneSided_GRIR_Daily(
            "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_GRIR_Daily",
            JobType.RECON, HandlerType.RECON),
    SuggestedClearing_SHERPAX(
            "CUS_FCA_TD_BSC_AccountItemLoop_SuggestedClearing_SHERPAX",
            JobType.RECON, HandlerType.RECON),
    SuggestedClearing_OneSided_WEA(
            "CUS_FCA_TD_BSC_AccountItemLoop_SuggestedClearing_OneSided_WEA",
            JobType.SUGGESTED, HandlerType.SUGGESTED);

    public final String name;
    public final JobType type;
    public final HandlerType handler;

    JobDefinitionRegistry(String name, JobType type, HandlerType handler)
    {
        this.name = name;
        this.type = type;
        this.handler = handler;
    }

    public static JobDefinitionRegistry fromName(String n)
    {
        for (JobDefinitionRegistry v : values()) if (v.name.equals(n)) return v;
        return null;
    }
}
