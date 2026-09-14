package com.redwood.scheduler.custom.kpi.kpi3.rtx;

public class RTXSchemas
{
    public static final RTXSchema CONDITIONAL =
            new RTXSchema(
                    "DocumentNo",
                    "Doc.Item",
                    "FiscalYear",
                    null,
                    "Rule_MatchKey");

    public static final RTXSchema DT =
            new RTXSchema(
                    "BELNR",
                    "BUZEI",
                    "GJAHR",
                    "RLDNR",
                    "StartNewTransaction");
}