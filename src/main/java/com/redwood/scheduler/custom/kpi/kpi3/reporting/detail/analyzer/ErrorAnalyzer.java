package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.analyzer;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.OperatorMessage;
import com.redwood.scheduler.api.model.enumeration.JobStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringJoiner;

public class ErrorAnalyzer
{
    public String getDescription(Job job)
    {
        if(job == null) return null;

        Job failedJob = findFailedLeaf(job);

        if(failedJob == null) return null;

        String operatorMessage = readOperatorMessage(failedJob);

        if (operatorMessage != null) return operatorMessage;

        String preRunning = readFile(failedJob, "stdprerunning.log");

        if (preRunning != null)
        {
            return preRunning;
        }

        String message = readFile(failedJob, "stderr.log");

        if(message != null) return message;

        return failedJob.getJobDefinition().getMasterJobDefinition().getName();
    }

    private String readOperatorMessage(Job failedJob)
    {
        StringJoiner sj = new StringJoiner(",");

        for(OperatorMessage om : failedJob.getOperatorMessages())
        {
            if(om.getFullText() != null && !om.getTitleText().isBlank())
            {
                sj.add(om.getTitleText());
            }
        }

        return sj.length() == 0 ? null : sj.toString();
    }


    private Job findFailedLeaf(Job job)
    {
        if(job.getStatus() == JobStatus.Error)
        {
            for(Job child : job.getChildJobs())
            {
                Job failed = findFailedLeaf(child);
                if(failed != null) return failed;
            }

            return job;
        }

        return null;
    }

    private String readFile(Job job,String fileName)
    {
        try
        {
            JobFile log = job.getJobFileByName(fileName);

            if(log == null) return null;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(log.getInputStream())))
            {
                String line;

                String candidate = null;

                while((line = br.readLine()) != null)
                {
                    line = line.trim();

                    if(line.isEmpty())
                    {
                        continue;
                    }

                    if(line.contains("Could not"))
                    {
                        return line;
                    }

                    if(line.contains("Mandatory parameter"))
                    {
                        return line;
                    }

                    if(candidate == null)
                    {
                        candidate = line;
                    }
                }

                return candidate;
            }
        }
        catch(Exception ignored)
        {
        }

        return null;
    }
}