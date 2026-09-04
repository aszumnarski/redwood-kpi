package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.model.FileKey;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec.getFileName;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;

public final class ResultFileWriter {
    private void writeItemsToFile(SchedulerSession session, String type, Map<String, List<String>> itemsMap, Job job)
            throws Exception
    {
        String fileName = getFileName(new FileKey(context.getParentDate(),context.getCompanyCode(), context.getAccountGroup(),"auto",type));
        boolean append = true;
        JobFile jf = job.getJobFileByName(fileName);
        if (jf == null)
        {
            jf = createJobFile(session,job, fileName);
            append = false;
        }
        try (FileOutputStream out = new FileOutputStream(jf.getFileName(), append))
        {
            for (String key : itemsMap.keySet())
            {
                int count = itemsMap.get(key).size();
                if (type.equals("cleared"))
                {
                    itemsCleared += count;
                }
                else if (type.equals("errors"))
                {
                    errors += count;
                }
                for (String item : itemsMap.get(key))
                {
                    write(out, item);
                }
            }
        }
    }
    private void writeErrorsAndClearedSeparately(SchedulerSession session,Job child, Map<String, List<String>> matchings,Job job)
            throws Exception
    {
        List<String> errorsList = RTXService.getErrorsRtx(child);
        Map<String, List<String>> errorsMap = new HashMap<>();
        Map<String, List<String>> clearedMap = new HashMap<>(matchings);
        for (String error : errorsList)
        {
            errorsMap.put(error, matchings.get(error));
            clearedMap.remove(error);
        }
        if(!errorsMap.isEmpty()) writeItemsToFile(session,"errors", errorsMap,job);
        if(!clearedMap.isEmpty()) writeItemsToFile(session,"cleared", clearedMap,job);
    }


}
