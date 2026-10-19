package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.repository;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.Table;
import com.redwood.scheduler.api.model.TableValue;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.*;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageKey;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.ScheduledCoverageEntry;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.TableRepository;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class ScheduledCoverageRepository {

    private final Map<String, Set<ReconConfiguration>> configurationCache = new HashMap<>();
    private final Map<String, Map<String, Set<TableValue>>> clearingTableCache = new HashMap<>();

    private record ClearingCoverageKey(
            String tableName,
            String companyCode) {
    }

    private final Map<ClearingCoverageKey, Set<String>> coveredItemsCache = new HashMap<>();

    private Set<String> getCoveredItems(
            SchedulerSession session,
            String tableName,
            String companyCode,
            Logger log) {

        ClearingCoverageKey key = new ClearingCoverageKey(tableName, companyCode);

        Set<String> coveredItems = coveredItemsCache.get(key);

        if (coveredItems != null) {
            log.info("Using cached clearing items for table=" + tableName + " company=" + companyCode);
            return coveredItems;
        }

        log.info(
                "Calculating clearing items for table="
                        + tableName
                        + " company="
                        + companyCode);

        coveredItems = new HashSet<>();

        Map<String, Set<TableValue>> rowsByKey =
                getRowsByKey(
                        session,
                        tableName,
                        log);

        for (Set<TableValue> values : rowsByKey.values()) {

            String rowCompanyCode = null;
            String accountItem = null;

            for (TableValue value : values) {

                switch (value.getColumnName()) {
                    case "BUKRS" ->
                            rowCompanyCode =
                                    value.getColumnValue();

                    case "Account_Item" ->
                            accountItem =
                                    value.getColumnValue();
                }
            }

            if (rowCompanyCode == null || accountItem == null) {
                continue;
            }

            if (!companyCode.equals(rowCompanyCode)) {
                continue;
            }

            coveredItems.add(accountItem);
        }

        coveredItems = Collections.unmodifiableSet(coveredItems);

        coveredItemsCache.put(key, coveredItems);

        return coveredItems;
    }

    private Set<ReconConfiguration> loadConfigurations(
            SchedulerSession session,
            String tableName, Logger log) {

        return configurationCache.computeIfAbsent(
                tableName,
                t -> {log.info("Loading configurations from " + t);

                    return new ReconConfigurationRepository().loadConfigurations(session, t);
                });
    }

    public Set<ScheduledCoverageEntry> findCoveredConfigurations(SchedulerSession session, Logger log) {

        log.info("Scheduled coverage calculation started");


        Map<ReconKey, MasterTableDefinition> definitions = indexByReconKey(new MasterTableDefinitionRepository().loadConfigurations(session));

        log.info("Loaded  " + definitions.size() + " master table definitions");

        Set<ScheduledCoverageEntry> entries = new HashSet<>();

        String query = "select j.* from Job j " +
                "join JobDefinition jd on j.JobDefinition = jd.UniqueId " +
                "where j.Status = 'S' and j.ParentJob is null " +
                "and (jd.Name like '%AUTOCLEAR%' or jd.Name like '%AutoClear%' or jd.Name like '%SuggestedClearing_OneSided%')";

        for (Job topLevelJob : session.executeObjectQuery(Job.TYPE, query)) {

            String jobDefinitionName = topLevelJob.getJobDefinition().getName();

            log.info("Analyzing jobId=" + topLevelJob.getJobId() + " definition=" + jobDefinitionName + " schedule=" + topLevelJob.getDescription());

            ReconKey reconKey = buildReconKey(topLevelJob);
            log.info("Resolved recon key " + reconKey);
            MasterTableDefinition definition = definitions.get(reconKey);


            if (definition == null) {
                log.warn("No master table definition found for recon key " + reconKey);
                continue;
            }

            switch (jobDefinitionName) {
                case "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_bySchedule" -> {

                    entries.addAll(createCoverageEntries(topLevelJob, findCoveredKeysIndia(session, topLevelJob, definition,log),log));
                }
                case "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_WEA" -> {
                    entries.addAll(createCoverageEntries(topLevelJob, findCoveredKeysFromClearingTable(session,topLevelJob, reconKey.companyCode(), reconKey.type(), definition.getOneSidedMasterTableName(),definition.getClearingRuleMasterTableAutoName(),log),log));

                }

                case "CUS_FCA_TD_BSC_AccountItemLoop_CONDITIONAL_AUTOCLEAR_REVIEW_WEA",
                     "CUS_FCA_TD_BSC_AccountItemLoop_SuggestedClearing_OneSided_WEA" -> {
                    entries.addAll(createCoverageEntries(topLevelJob, findCoveredKeysFromClearingTable(session,topLevelJob, reconKey.companyCode(), reconKey.type(), definition.getOneSidedMasterTableName(),definition.getClearingRuleMasterTableSuggestedName(),log),log));

                }

                case "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_US" -> {
                    entries.addAll(createCoverageEntries(topLevelJob, findCoveredKeysCash(session, definition,log),log));
                }

                default -> throw new IllegalArgumentException("Unknown definition "+ jobDefinitionName);
            }
        }

        log.info("Generated " + entries.size() + " scheduled coverage entries");
        return entries;
    }

    private Set<CoverageKey> findCoveredKeysCash(SchedulerSession session, MasterTableDefinition definition,Logger log) {
        String definitionCompanyCode = definition.getReconKey().companyCode();
        String definitionReconType = definition.getReconKey().type();
        Set<ReconConfiguration> configurations = loadConfigurations(session, definition.getOneSidedMasterTableName(),log);
        log.info("Using " + configurations.size() + " configurations from " + definition.getOneSidedMasterTableName());
        Set<ReconConfiguration> companyCodeConfigurations =
                configurations.stream()
                        .filter(c -> definitionCompanyCode.equals(c.getCompanyCode()) && definitionReconType.equals(c.getReconType()))
                        .collect(Collectors.toSet());

        log.info("After filtering company=" + definitionCompanyCode + ", type=" + definitionReconType + ", frequencies={} -> " + companyCodeConfigurations.size() + " configurations");

        Set<CoverageKey> result = new HashSet<>();

        for (ReconConfiguration config : companyCodeConfigurations) {

            result.add(new CoverageKey(definitionCompanyCode, config.getAccountItem()));

        }

        return result;


    }

    private Set<CoverageKey> findCoveredKeysFromClearingTable(SchedulerSession session, Job topLevelJob,String topLevelCompanyCode, String type, String oneSidedTableName, String clearingTableName,Logger log){
        Set<String> frequencies = getApplicableFrequencies(topLevelJob);
        log.info("Applicable frequencies for " + topLevelJob.getJobDefinition().getName() + " are " + frequencies);
        Set<ReconConfiguration> configurations = loadConfigurations(session, oneSidedTableName,log);
        log.info("Using " + configurations.size() + " configurations from " + oneSidedTableName);
        Set<ReconConfiguration> companyCodeConfigurations =
                configurations.stream()
                        .filter(c -> topLevelCompanyCode.equals(c.getCompanyCode()) && type.equals(c.getReconType()) && frequencies.contains(c.getFrequency()))
                        .collect(Collectors.toSet());

        log.info("After filtering company=" + topLevelCompanyCode + ", type=" + type + ", frequencies=" + frequencies + " -> " + companyCodeConfigurations.size() + " configurations");


        Set<String> accountItemsClearing = getCoveredItems(session, clearingTableName, topLevelCompanyCode, log);

        log.info("Found " + accountItemsClearing.size() + " clearing account items");


        Set<CoverageKey> result = new HashSet<>();
        Set<String> excluded = new HashSet<>();
        for (ReconConfiguration config : companyCodeConfigurations) {

            if (accountItemsClearing.contains(config.getAccountItem())) {
                result.add(new CoverageKey(topLevelCompanyCode, config.getAccountItem()));
            }else{
                excluded.add(config.getAccountItem());
            }

        }

        log.info("Excluded " + excluded.size() + " account items");

        log.info("Coverage result: " + companyCodeConfigurations.size() + " configuration items, " + accountItemsClearing.size() + " clearing items, " + result.size() + " covered items");

        return result;
    }


    private Set<String> getApplicableFrequencies(Job topLevelJob) {

        DateTimeZone schedule = topLevelJob.getScheduledStartTime();
        int month = Integer.parseInt(schedule.expression("add 3 months").toFormattedString("MM"));

        Set<String> result = new HashSet<>();

        result.add("Daily");
        result.add("Weekly");
        result.add("Monthly");

        if(isQuarterEnd(month)) result.add("Quarterly");

        if(isYearEnd(month)) result.add("Yearly");

        return result;

    }

    private boolean isYearEnd(Integer month) {

        return month == 12;
    }

    private boolean isQuarterEnd(Integer month) {
        return(month == 3 || month == 6 || month == 9 || month == 12);
    }




    private ReconKey buildReconKey(Job topLevelJob) {
        String companyCode = JobParameterHelper.getParameter(topLevelJob, "BUKRS");
        String reconType = JobParameterHelper.getParameter(topLevelJob, "RECON_TYPE");
        String sapSystems = JobParameterHelper.getParameter(topLevelJob, "SAP_SYSTEMS");
        String client = JobParameterHelper.getParameter(topLevelJob, "CLIENT");
        return new ReconKey(sapSystems, client, companyCode, normalize(reconType));
    }

    private Set<ScheduledCoverageEntry> createCoverageEntries(Job topLevelJob, Set<CoverageKey> coverageKeys,Logger log) {
        Set<ScheduledCoverageEntry> result = new HashSet<>();

        long jobId = topLevelJob.getJobId();
        String jobDefinitionName = topLevelJob.getJobDefinition().getName();
        String scheduleName = topLevelJob.getDescription();
        DateTimeZone nextRunTime = topLevelJob.getScheduledStartTime();
        for (CoverageKey coverageKey : coverageKeys) {
            result.add(new ScheduledCoverageEntry(coverageKey, jobId, jobDefinitionName, scheduleName, nextRunTime));
        }

        log.info("Job " + topLevelJob.getJobId() + " (" + topLevelJob.getJobDefinition().getName() + ") covers " + coverageKeys.size() + " account items");
        return result;
    }

    private Set<CoverageKey> findCoveredKeysIndia(SchedulerSession session, Job topLevelJob, MasterTableDefinition definition,Logger log) {
        String schedule = JobParameterHelper.getParameter(topLevelJob, "IN_SCHEDULE");
        log.info("Applicable schedule for " + topLevelJob.getJobDefinition().getName() + " is " + schedule);


        String definitionCompanyCode = definition.getReconKey().companyCode();
        Set<CoverageKey> result = new HashSet<>();


        Map<String, Set<TableValue>> rowsByKey = getRowsByKey(session, definition.getClearingRuleMasterTableName(),log);


        log.info("Loaded " + rowsByKey.size() + " configurations from " + definition.getClearingRuleMasterTableName());
        for (Set<TableValue> values : rowsByKey.values()) {

            String companyCode = null;
            String accountItem = null;
            String notes = null;

            for (TableValue value : values) {

                String columnName = value.getColumnName();

                switch (columnName) {
                    case "BUKRS" -> companyCode = value.getColumnValue();
                    case "Account_Item" -> accountItem = value.getColumnValue();
                    case "Notes" -> notes = value.getColumnValue();
                }

            }

            if (companyCode == null || accountItem == null) continue;

            if (!definitionCompanyCode.equals(companyCode)) continue;


            CoverageKey key = new CoverageKey(definitionCompanyCode, accountItem);

            if (schedule == null || schedule.isEmpty()) {
                result.add(key);
            } else {
                if (notes != null) {
                    for (String note : notes.split(",", -1)) {
                        if (schedule.trim().equals(note.trim())) {
                            result.add(key);
                            break;
                        }
                    }
                }

            }
        }

        log.info("After filtering company=" + definitionCompanyCode + ", type=" + definition.getReconKey().type() + ", schedule=" + schedule + " -> " + result.size() + " configurations");

        return result;
    }

    private Map<String, Set<TableValue>> getRowsByKey(
            SchedulerSession session,
            String tableName,Logger log) {

        return clearingTableCache.computeIfAbsent(
                tableName,
                t -> {

                    log.info("Loading clearing table " + t);

                    Table table = TableRepository.getTableByName(session, t);

                    log.info("Loaded clearing table " + t);

                    if (table == null) {
                        return Collections.emptyMap();
                    }

                    return groupRowsByKey(table);
                });
    }

    private Map<String, Set<TableValue>> groupRowsByKey(Table table) {

        Map<String, Set<TableValue>> rowsByKey = new HashMap<>();

        for (TableValue tv : table.getTableValues()) {
            rowsByKey.computeIfAbsent(tv.getKey(), k -> new HashSet<>()).add(tv);
        }

        return rowsByKey;
    }

    private Map<ReconKey, MasterTableDefinition> indexByReconKey(Set<MasterTableDefinition> definitions) {
        Map<ReconKey, MasterTableDefinition> result = new HashMap<>();
        for (MasterTableDefinition definition : definitions) {
            result.put(definition.getReconKey(), definition);
        }
        return result;
    }

    private static String normalize(String value) {

        if (value == null) return null;

        value = value.replace("_", "");

        return switch (value) {
            case "BANK" -> "OIM";
            case "BANK2" -> "SFSBANK";
            default -> value;
        };
    }

}
