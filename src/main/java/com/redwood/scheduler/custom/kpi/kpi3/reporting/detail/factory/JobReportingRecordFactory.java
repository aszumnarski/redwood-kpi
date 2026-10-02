package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.factory;

import com.redwood.scheduler.api.exception.SchedulerAPIPersistenceException;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.date.PeriodUtil;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileAllocation;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemContext;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.MasterTableRepository;

import java.util.*;


public class JobReportingRecordFactory
{
    private final Map<String, String> accountTypeCache = new HashMap<>();

    public JobReportingRecord create(
            SchedulerSession session,
            AccountItemContext context,
            CollectorStats stats,
            FileAllocation allocation,
            JobType jobType,
            Set<String> userIds,
            Long linkId)
    {
        JobReportingRecord record = new JobReportingRecord();

        record.setPeriod(PeriodUtil.getPeriod(context.getParentDate()));
        record.setJobId(context.getId());
        record.setCompanyCode(context.getCompanyCode());
        record.setAccountItem(context.getAccountItem());
        record.setAccountType(getAccountType(session,context.getCompanyCode(),context.getAccountItem()));
        record.setClearingType(jobType);
        record.setJobName(context.getName());
        record.setRunDate(context.getRunStartDate());
        record.setScheduleDate(context.getParentDate());
        record.setStatus(context.getStatus());
        //errorDescription
        record.setUserIds(userIds == null ? Collections.emptySet() : new HashSet<>(userIds));
        record.setLinkId(linkId);
        record.setCounts(stats.getCounts());
        record.setSourceFiles(allocation);

        return record;
    }

    private String getAccountType(
            SchedulerSession session,
            String companyCode,
            String accountItem)
    {
        String key = companyCode + ":" + accountItem;

        return accountTypeCache.computeIfAbsent(key,
                k -> {
                    try {
                        return MasterTableRepository.getAccountType(session, companyCode, accountItem);
                    } catch (SchedulerAPIPersistenceException e) {
                        throw new RuntimeException("Cannot determine account type for " + companyCode + ":" + accountItem, e);
                    }
                });
    }
}

