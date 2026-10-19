package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.factory;

import com.redwood.scheduler.api.exception.SchedulerAPIPersistenceException;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.date.PeriodUtil;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileAllocation;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemContext;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.*;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.analyzer.ErrorAnalyzer;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.util.*;


public class JobReportingRecordFactory
{
    private final ErrorAnalyzer errorAnalyzer = new ErrorAnalyzer();

    private static Map<String, Set<String>> accountTypeIndex;

    public JobReportingRecord create(
            SchedulerSession session,
            AccountItemContext context,
            CollectorStats stats,
            FileAllocation allocation,
            JobType jobType,
            Set<String> userIds,
            Long linkId,
            Logger log) {

        JobReportingRecord record = new JobReportingRecord();

        record.setPeriod(PeriodUtil.getPeriod(context.getParentDate()));
        record.setJobId(context.getId());
        record.setCompanyCode(context.getCompanyCode());
        record.setAccountItem(context.getAccountItem());
        record.setAccountType(getAccountType(session,context.getCompanyCode(),context.getAccountItem(),log));
        record.setClearingType(jobType);
        //record.setJobName(context.getName());
        record.setJobName(context.getJobDescription());
        record.setRunDate(context.getRunStartDate());
        record.setScheduleDate(context.getParentDate());
        record.setStatus(context.getStatus());
        record.setErrorDescription(errorAnalyzer.getDescription(context.getJob()));
        record.setUserIds(userIds == null ? Collections.emptySet() : new HashSet<>(userIds));
        record.setLinkId(linkId);
        record.setCounts(stats.getCounts());
        record.setSourceFiles(allocation);

        return record;
    }

    private String getAccountType(
            SchedulerSession session,
            String companyCode,
            String accountItem,
            Logger log) {

        try {

            if (accountTypeIndex == null) {

                accountTypeIndex = buildAccountTypeIndex(session, log);
                long duplicates = accountTypeIndex.entrySet().stream().filter(e -> e.getValue().size() > 1).count();
                log.info("AccountType duplicate keys: " + duplicates);

            }

            Set<String> types = accountTypeIndex.get(companyCode + ":" + accountItem);

            if (types == null || types.isEmpty()) {
                return "UNKNOWN";
            }

            if (types.size() > 1) {
                log.info("Multiple account types found for " + companyCode + ":" + accountItem + " -> " + types);
            }

            return types.iterator().next();

        } catch (SchedulerAPIPersistenceException e) {

            throw new RuntimeException("Cannot determine account type for " + companyCode + ":" + accountItem, e);
        }
    }

    private static Map<String, Set<String>> buildAccountTypeIndex(SchedulerSession session, Logger log)
            throws SchedulerAPIPersistenceException {

        MasterTableDefinitionRepository masterTableDefinitionRepository = new MasterTableDefinitionRepository();
        ReconConfigurationRepository reconConfigurationRepository = new ReconConfigurationRepository();

        ReconConfigurationProvider provider = new ReconConfigurationProvider(masterTableDefinitionRepository, reconConfigurationRepository);

        Set<ReconConfiguration> configurations = provider.loadReconConfigurations(session,log);

        Map<String, Set<String>> index = new HashMap<>();

        for (ReconConfiguration configuration : configurations) {

            String key = configuration.getCompanyCode() + ":" + configuration.getAccountItem();
            index.computeIfAbsent(key, k -> new HashSet<>()).add(ReconTypeNormalizer.normalize(configuration.getAccountCategory()));
        }

        return index;
    }


}

