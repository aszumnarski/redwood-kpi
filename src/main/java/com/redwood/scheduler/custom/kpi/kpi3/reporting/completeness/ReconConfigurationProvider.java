package com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class ReconConfigurationProvider {

    private final MasterTableDefinitionRepository masterTableDefinitionRepository;
    private final ReconConfigurationRepository reconConfigurationRepository;

    public ReconConfigurationProvider(MasterTableDefinitionRepository masterTableDefinitionRepository, ReconConfigurationRepository reconConfigurationRepository) {

        this.masterTableDefinitionRepository = masterTableDefinitionRepository;
        this.reconConfigurationRepository = reconConfigurationRepository;
    }

    public Set<ReconConfiguration> loadReconConfigurations(SchedulerSession session, Logger log) {

        Set<MasterTableDefinition> definitions = masterTableDefinitionRepository.loadClearingConfigurations(session);
        Set<ReconConfiguration> allConfigurations = new HashSet<>();
        Set<String> tableNames = definitions.stream().map(MasterTableDefinition::getOneSidedMasterTableName).filter(Objects::nonNull).collect(Collectors.toSet());

        for(String tableName : tableNames){
            allConfigurations.addAll(reconConfigurationRepository.loadConfigurations(session, tableName));
        }

        Map<String, Set<ReconConfiguration>> index = buildIndex(allConfigurations);

        Set<ReconConfiguration> matched = new HashSet<>();

        Set<String> missingKeys = new TreeSet<>();

        for (MasterTableDefinition definition : definitions) {

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

    public void printStatistics(Set<ReconConfiguration> configurations,Logger log) {

        printDistribution(
                "Frequency",
                configurations.stream()
                        .collect(Collectors.groupingBy(
                                c -> valueOrNull(c.getFrequency()),
                                TreeMap::new,
                                Collectors.counting())),log);

        printDistribution(
                "Account Category",
                configurations.stream()
                        .collect(Collectors.groupingBy(
                                c -> valueOrNull(c.getAccountCategory()),
                                TreeMap::new,
                                Collectors.counting())),log);

        printDistribution(
                "Recon Type",
                configurations.stream()
                        .collect(Collectors.groupingBy(
                                c -> ReconTypeNormalizer.normalize(
                                        c.getReconType()),
                                TreeMap::new,
                                Collectors.counting())),log);

        printDistribution(
                "Due Date",
                configurations.stream()
                        .collect(Collectors.groupingBy(
                                c -> valueOrNull(c.getDueDate()),
                                TreeMap::new,
                                Collectors.counting())),log);

        printDistribution(
                "Source Table",
                configurations.stream()
                        .collect(Collectors.groupingBy(
                                c -> valueOrNull(c.getSourceMasterTable()),
                                TreeMap::new,
                                Collectors.counting())),log);

        printDistribution(
                "Company Code",
                configurations.stream()
                        .collect(Collectors.groupingBy(
                                ReconConfiguration::getCompanyCode,
                                TreeMap::new,
                                Collectors.counting())),log);

        printDistribution(
                "Frequency + Due Date",
                configurations.stream()
                        .collect(Collectors.groupingBy(
                                c -> c.getFrequency()
                                        + " | "
                                        + c.getDueDate(),
                                TreeMap::new,
                                Collectors.counting())),log);


        log.info("Null frequency: "
                + configurations.stream()
                .filter(c -> c.getFrequency() == null)
                .count());

        log.info("Null dueDate: "
                + configurations.stream()
                .filter(c -> c.getDueDate() == null)
                .count());

        log.info("Null category: "
                + configurations.stream()
                .filter(c -> c.getAccountCategory() == null)
                .count());

        log.info("Null reconType: "
                + configurations.stream()
                .filter(c -> c.getReconType() == null)
                .count());

        configurations.stream()
                .filter(c -> c.getDueDate() == null)
                .limit(50)
                .forEach(c -> log.info(c.toString()));


    }

    private String valueOrNull(String value) {
        return value == null ? "<NULL>" : value;
    }

    private void printDistribution(
            String title,
            Map<String, Long> distribution,Logger log) {

        log.info("");
        log.info("==== " + title + " ====");

        distribution.forEach((key, count) ->
                log.info(key + " -> " + count));
    }

    private Map<String, Set<ReconConfiguration>> buildIndex(
            Set<ReconConfiguration> configurations) {

        return configurations.stream()
                .collect(Collectors.groupingBy(
                        ReconConfiguration::getLookupKey,
                        Collectors.toSet()));
    }

    private String getLookupKey(
            MasterTableDefinition definition) {

        ReconKey reconKey = definition.getReconKey();
        return reconKey.companyCode() + "|" + ReconTypeNormalizer.normalize(reconKey.type());
    }




}
