package com.redwood.scheduler.custom.kpi.kpi3.file;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.model.ClearingResult;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXSchema;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXService;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClearingResultProcessor
{
    private final ResultFileWriter fileWriter;
    private  final RTXSchema schema;

    public ClearingResultProcessor(ResultFileWriter fileWriter, RTXSchema schema)
    {
        this.fileWriter = fileWriter;
        this.schema = schema;
    }

    public ClearingResult process(
            SchedulerSession session,
            Job clearingJob,
            Job outputJob,
            PrintWriter writer)
            throws Exception
    {

        String okLines = JobParameterHelper.getParameter(clearingJob, "OUT_DATA_OK_RTX");
        String errorLines = JobParameterHelper.getParameter(clearingJob, "OUT_DATA_ERROR_RTX");

        Map<String, List<String>> matchings = RTXService.groupByKey(clearingJob, "IN_DATA_RTX", schema, writer);

        int proposed = fileWriter.writeItemsToFile(session,outputJob,"proposed", matchings);

        if (errorLines == null)
        {
            if (!matchings.isEmpty()) fileWriter.writeItemsToFile(session, outputJob, "cleared", matchings);
            return new ClearingResult(proposed, proposed, 0);
        }

        if (okLines == null)
        {
            if (!matchings.isEmpty()) fileWriter.writeItemsToFile(session, outputJob, "errors", matchings);
            return new ClearingResult(proposed, 0, proposed);
        }

        List<String> errorsList = RTXService.getErrorsRtx(clearingJob,"OUT_DATA_ERROR_RTX", schema.groupingColumn());

        Map<String, List<String>> errorsMap = new HashMap<>();
        Map<String, List<String>> clearedMap = new HashMap<>(matchings);

        for (String error : errorsList)
        {
            errorsMap.put(error, matchings.get(error));
            clearedMap.remove(error);
        }

        int errorsCount = fileWriter.writeItemsToFile(session,outputJob,"errors", errorsMap);
        int clearedCount = fileWriter.writeItemsToFile(session,outputJob,"cleared", clearedMap);

        if (proposed != clearedCount + errorsCount)
        {
            writer.println("WARNING: proposed=" + proposed + ", cleared=" + clearedCount + ", errors=" + errorsCount);
        }

        return new ClearingResult(proposed, clearedCount, errorsCount);
    }


}
