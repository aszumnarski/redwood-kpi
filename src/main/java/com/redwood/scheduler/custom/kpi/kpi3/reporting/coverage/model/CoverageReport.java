package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoverageReport {

    private Map<CoverageKey, CoverageStatus> statuses = new LinkedHashMap<>();

    public CoverageReport() {
    }

    public Map<CoverageKey, CoverageStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(
            Map<CoverageKey, CoverageStatus> statuses) {

        this.statuses = statuses;
    }

    public Collection<CoverageStatus> getCoverageStatuses() {
        return statuses.values();
    }

    public void put(
            CoverageKey key,
            CoverageStatus status) {

        statuses.put(key, status);
    }

    public boolean contains(CoverageKey key) {
        return statuses.containsKey(key);
    }

    public CoverageStatus get(CoverageKey key) {
        return statuses.get(key);
    }

    public CoverageStatus getOrCreate(
            CoverageKey key) {

        return statuses.computeIfAbsent(
                key,
                CoverageStatus::new);
    }

    public int size() {
        return statuses.size();
    }

    public Collection<CoverageStatus> values() {
        return statuses.values();
    }

    public long getConfiguredCount() {

        return statuses.values()
                .stream()
                .filter(CoverageStatus::isConfigured)
                .count();
    }

    public long getScheduledCount() {

        return statuses.values()
                .stream()
                .filter(CoverageStatus::isScheduled)
                .count();
    }

    public long getExecutedCount() {

        return statuses.values()
                .stream()
                .filter(CoverageStatus::isExecuted)
                .count();
    }

    @Override
    public String toString() {

        return "CoverageReport{" +
                "statuses=" + statuses.size() +
                ", configured=" + getConfiguredCount() +
                ", scheduled=" + getScheduledCount() +
                ", executed=" + getExecutedCount() +
                '}';
    }

    public Stream<CoverageStatus> stream() {
        return statuses.values().stream();
    }

    public long getConfiguredNotExecutedCount() {
        return stream()
                .filter(CoverageStatus::isConfigured)
                .filter(s -> !s.isExecuted())
                .count();
    }

    public long getConfiguredNotScheduledCount() {

        return stream()
                .filter(CoverageStatus::isConfigured)
                .filter(s -> !s.isScheduled())
                .count();
    }

    public long getScheduledNotExecutedCount() {

        return stream()
                .filter(CoverageStatus::isScheduled)
                .filter(s -> !s.isExecuted())
                .count();
    }

    public long getUnexpectedExecutionCount() {

        return stream()
                .filter(s -> !s.isConfigured())
                .filter(CoverageStatus::isExecuted)
                .count();
    }

    public long getConfiguredScheduledExecutedCount() {

        return stream()
                .filter(CoverageStatus::isConfigured)
                .filter(CoverageStatus::isScheduled)
                .filter(CoverageStatus::isExecuted)
                .count();
    }

    public long getConfiguredScheduledNotExecutedCount() {

        return stream()
                .filter(CoverageStatus::isConfigured)
                .filter(CoverageStatus::isScheduled)
                .filter(s -> !s.isExecuted())
                .count();
    }

    public long getConfiguredExecutedNotScheduledCount() {

        return stream()
                .filter(CoverageStatus::isConfigured)
                .filter(s -> !s.isScheduled())
                .filter(CoverageStatus::isExecuted)
                .count();
    }

    public long getConfiguredOnlyCount() {

        return stream()
                .filter(CoverageStatus::isConfigured)
                .filter(s -> !s.isScheduled())
                .filter(s -> !s.isExecuted())
                .count();
    }

    public long getTotalExecutionCount() {

        return stream()
                .mapToLong(CoverageStatus::getExecutionCount)
                .sum();
    }

    public Map<String, Long> getGapCountByCategory() {
        return stream()
                .filter(CoverageStatus::isConfigured)
                .filter(s -> !s.isScheduled())
                .filter(s -> !s.isExecuted())
                .collect(Collectors.groupingBy(
                        CoverageStatus::getCategory,
                        TreeMap::new,
                        Collectors.counting()));
    }

    public Map<String, Long> getClearingGapCountByFrequency() {

        return stream()
                .filter(CoverageStatus::isConfigured)
                .filter(s -> !s.isScheduled())
                .filter(s -> !s.isExecuted())
                .filter(s -> "Clearing".equals(s.getCategory()))
                .collect(Collectors.groupingBy(
                        CoverageStatus::getFrequency,
                        TreeMap::new,
                        Collectors.counting()));
    }

    public Map<String, Long> getClearingGapCountByCompany() {

        return stream()
                .filter(CoverageStatus::isConfigured)
                .filter(s -> !s.isScheduled())
                .filter(s -> !s.isExecuted())
                .filter(s -> "Clearing".equals(s.getCategory()))
                .collect(Collectors.groupingBy(
                        CoverageStatus::getCompanyCode,
                        TreeMap::new,
                        Collectors.counting()));
    }

}
