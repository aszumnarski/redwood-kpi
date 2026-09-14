package com.redwood.scheduler.custom.kpi.kpi3.file;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemContext;

import java.io.FileOutputStream;
import java.util.*;

import com.redwood.scheduler.custom.kpi.kpi3.monitoring.FileStatistics;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;

public class ResultFileWriter
{
    private final AccountItemContext context;
    private final String type;
    private static final FileStatistics FILE_STATS = new FileStatistics();

    public ResultFileWriter()
    {
        this.context = null;
        this.type = null;
    }

    public ResultFileWriter(AccountItemContext context, String type)
    {
        this.context = context;
        this.type = type;
    }

    public String buildFileName(String name){
        return FileKeyCodec.getFileName(new FileKey(context.getParentDate(), context.getCompanyCode(), context.getAccountGroup(), type, name));
    }

    public int writeItemsToNamedFile(SchedulerSession session, Job job, String fileName, Collection<String> items) throws Exception
    {
        boolean append = true;

        FILE_STATS.lookup();

        JobFile jobFile = job.getJobFileByName(fileName);

        if (jobFile == null)
        {
            FILE_STATS.miss();
            jobFile = createJobFile(session, job, fileName);
            append = false;
        }else {
            FILE_STATS.hit();
        }

        int count = 0;

        try (FileOutputStream out = new FileOutputStream(jobFile.getFileName(), append))
        {
            for (String item : items)
            {
                write(out, item);
                count++;
            }
        }
        FILE_STATS.addRows(fileName, count);
        return count;
    }

    public int writeItemsToFile(SchedulerSession session, Job job, String resultType, Collection<String> items) throws Exception
    {
        String fileName = buildFileName(resultType);
        return writeItemsToNamedFile(session,job,fileName,items);
    }

    public int writeItemsToFile(SchedulerSession session, Job job, String resultType, Map<String, List<String>> itemsMap)
            throws Exception
    {
        List<String> items = new ArrayList<>();

        for (List<String> values : itemsMap.values())
        {
            items.addAll(values);
        }

        return writeItemsToFile(session, job, resultType, items);
    }


    public static FileStatistics getStatistics()
    {
        return FILE_STATS;
    }

}





