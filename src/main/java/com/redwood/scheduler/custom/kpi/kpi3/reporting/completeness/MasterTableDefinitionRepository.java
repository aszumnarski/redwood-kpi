package com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.Table;
import com.redwood.scheduler.api.model.TableValue;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.TableRepository;

import java.util.*;
import java.util.stream.Collectors;

public class MasterTableDefinitionRepository {

    private static final String MASTERTABLE_MAPPING = "CUS_TABLE_BSC_MASTERTABLE_MAPPING";

    private final MasterTableDefinitionFactory factory = new MasterTableDefinitionFactory();

    public Set<MasterTableDefinition> loadConfigurations(SchedulerSession session) {

        Table repository = TableRepository.getTableByName(session,MASTERTABLE_MAPPING);

        if (repository == null) {
            return Collections.emptySet();
        }

        Map<String, Set<TableValue>> rowsByKey = groupRowsByKey(repository);

        Set<MasterTableDefinition> result = new HashSet<>();

        for (Map.Entry<String, Set<TableValue>> entry : rowsByKey.entrySet()) {

            result.add(factory.create(entry.getKey(), entry.getValue()));

        }

        return result;

    }

    public Set<MasterTableDefinition> loadClearingConfigurations(
            SchedulerSession session) {

        return loadConfigurations(session)
                .stream()
                .filter(d -> "OIM".equals(d.getReconKey().type()) || "SFSBANK".equals(d.getReconKey().type()))
                .collect(Collectors.toSet());
    }

    private Map<String, Set<TableValue>> groupRowsByKey(Table table) {

        Map<String, Set<TableValue>> rowsByKey = new HashMap<>();

        for (TableValue tv : table.getTableValues()) {
            rowsByKey.computeIfAbsent(tv.getKey(), k -> new HashSet<>()).add(tv);
        }

        return rowsByKey;
    }

}
