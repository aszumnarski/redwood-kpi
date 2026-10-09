package com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness;

import com.redwood.scheduler.api.exception.SchedulerAPIPersistenceException;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.MasterTableRepository;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class ReconConfigurationProvider {

    private final MasterTableRepository masterTableRepository;
    private final ReconConfigurationRepository reconConfigurationRepository;

    public ReconConfigurationProvider(MasterTableRepository masterTableRepository, ReconConfigurationRepository reconConfigurationRepository) {

        this.masterTableRepository = masterTableRepository;
        this.reconConfigurationRepository = reconConfigurationRepository;
    }

    public Set<ReconConfiguration> loadReconConfigurations(SchedulerSession session, Logger log) throws SchedulerAPIPersistenceException {
        Set<CompletenessDefinition> definitions = masterTableRepository.getDefinitions(session);

        Set<ReconConfiguration> allConfigurations = new HashSet<>();

        Set<String> tableNames = definitions.stream().map(CompletenessDefinition::getSourceMasterTable).collect(Collectors.toSet());

        for(String tableName : tableNames){
            allConfigurations.addAll(reconConfigurationRepository.loadConfigurations(session, tableName));
        }

        Map<String, Set<ReconConfiguration>> index = buildIndex(allConfigurations);

        Set<ReconConfiguration> matched = new HashSet<>();

        Set<String> missingKeys = new TreeSet<>();

        for (CompletenessDefinition definition : definitions) {

            String key = getLookupKey(definition);
            if (!index.containsKey(key)) missingKeys.add(key);
            matched.addAll(index.getOrDefault(key, Collections.emptySet()));
        }

        Map<String, Set<String>> typesByAccountItem = new HashMap<>();

        for (ReconConfiguration configuration : matched) {

            String key = configuration.getCompanyCode() + ":" + configuration.getAccountItem();

            typesByAccountItem.computeIfAbsent(key, k -> new HashSet<>()).add(configuration.getReconType());
        }

        Set<ReconConfiguration> excluded = new HashSet<>(allConfigurations);
        excluded.removeAll(matched);

        Map<String, Long> excludedByLookupKey = excluded.stream().collect(Collectors.groupingBy(ReconConfiguration::getLookupKey, Collectors.counting()));

        log.info("Definitions: " + definitions.size());
        log.info("Configurations: " + allConfigurations.size());
        log.info("Matched: " + matched.size());
        log.info("Excluded: " + excluded.size());
        log.info("MissingKeys: " + missingKeys.size());
        log.info("ExcludedByLookupKey: " + excludedByLookupKey.size());
        log.info("TypesByAccountItem: " + typesByAccountItem.size());
        log.info("====================================================================");
        log.debug(matched.toString());
        log.debug("====================================================================");
        log.debug(excluded.toString());
        log.debug("====================================================================");
        log.debug(missingKeys.toString());
        log.debug("====================================================================");
        log.debug(excludedByLookupKey.toString());
        log.debug("====================================================================");
        log.debug(typesByAccountItem.toString());
        log.debug("====================================================================");
        return matched;
    }

    private Map<String, Set<ReconConfiguration>> buildIndex(
            Set<ReconConfiguration> configurations) {

        return configurations.stream()
                .collect(Collectors.groupingBy(
                        ReconConfiguration::getLookupKey,
                        Collectors.toSet()));
    }

    private String getLookupKey(
            CompletenessDefinition definition) {

        ReconKey reconKey = definition.getReconKey();
        return reconKey.companyCode() + "|" + ReconTypeNormalizer.normalize(reconKey.type());
    }




}
