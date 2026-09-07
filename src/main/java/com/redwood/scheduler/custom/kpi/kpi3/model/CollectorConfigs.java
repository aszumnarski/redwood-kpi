package com.redwood.scheduler.custom.kpi.kpi3.model;

import java.util.List;
import java.util.Set;

public final class CollectorConfigs
{
    public static final Set<String> baseParentsLedger = Set.of("CUS_TD_BSC_AUTOCLEAR_WEA","CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_Original","CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH","CUS_TD_BSC_AUTOCLEAR_SHERPAX_new","CUS_TD_BSC_AUTOCLEAR_Cashmatching_E1P_US","CUS_SPD_BSC_AUTOCLEAR_SHERPAX_LDGRP","CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_LDGRP_US");
    public static final Set<String> dtParentsLedger = Set.of("CUS_SPD_BSC_AUTOCLEAR_RULES_WEA_new","CUS_SPD_BSC_AUTOCLEAR_RULES_Sherpax_Global_new","CUS_SPD_BSC_AUTOCLEAR_RULES_Sherpax_Focus4_new","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_Phase5");
    public static final Set<String> collectParentsAuto = Set.of("CUS_SPD_BSC_AUTOCLEAR_RULES_WEA_new","CUS_SPD_BSC_AUTOCLEAR_RULES_Sherpax_Global_new","CUS_SPD_BSC_AUTOCLEAR_RULES_Sherpax_Focus4_new","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_Phase4","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_SOFOM","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_US");
    public static final Set<String> baseParentsAuto = Set.of("CUS_TD_BSC_AUTOCLEAR_WEA","CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_Original","CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH","CUS_TD_BSC_AUTOCLEAR_SHERPAX_new","CUS_TD_BSC_AUTOCLEAR_Cashmatching_E1P_US");
    public static final Set<String> totalOpenItemsParentsOld = Set.of("CUS_TD_BSC_AUTOCLEAR_WEA","CUS_TD_BSC_AUTOCLEAR_CB_IN");

    public static final CollectorConfig AUTO = new CollectorConfig(baseParentsAuto,collectParentsAuto,true);

    public static final CollectorConfig AUTO_OLD = new CollectorConfig(totalOpenItemsParentsOld,null,true);

    public static final CollectorConfig LEDGER = new CollectorConfig(baseParentsLedger,dtParentsLedger,true);
}
