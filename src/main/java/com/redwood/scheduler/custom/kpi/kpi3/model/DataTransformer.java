package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.rtx.RTXReader;
import com.redwood.scheduler.api.rtx.RTXRow;

import java.util.Set;
import java.util.HashSet;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec.getFileName;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;

class DataTransformer
{
    private DateTimeZone runStartDate;
    private DateTimeZone runEndDate;
    private DateTimeZone parentDate;
    private String status;
    private String name;
    private Long id;
    private int ruleSet1;
    private int autoClear;
    private Set<String> autoClearSet;
    private int suggestedClear;
    private Set<String> suggestedClearSet;
    private int totalCollected;
    private Set<String> totalCollectedSet;
    private Job j;
    private int skippedCount = 0;
    private String companyCode;
    private String accountGroup;
    private String type;

    public DataTransformer(SchedulerSession session,Job j,boolean collectRuleSet1,PrintWriter p,Job job, String companyCode, String accountGroup, String type, DateTimeZone parentDate)
            throws Exception
    {
        this.parentDate = parentDate;
        this.j = j;
        this.companyCode = companyCode;
        this.accountGroup = accountGroup;
        this.type = type;
        id = j.getJobId();
        p.println("DataTransformer created - " + id);
        runStartDate = j.getRunStart();
        runEndDate = j.getRunEnd();
        status = j.getStatus().getTranslationEN();
        name = j.getJobDefinition().getMasterJobDefinition().getName();
        if(!status.equals("Completed"))
        {
            ruleSet1 = 0;
            autoClear = 0;
            suggestedClear = 0;
            totalCollected = 0;
        }
        else
        {
            ruleSet1 = j.getJobParameterByName("RuleSet1RowCount") != null ? j.getJobParameterByName("RuleSet1RowCount").getOutValueNumber().intValue() : 0;
            if(collectRuleSet1) printRuleSet1Items(session,j,p,job);
            autoClear = j.getJobParameterByName("AutoClearRowCount") != null ? j.getJobParameterByName("AutoClearRowCount").getOutValueNumber().intValue() : 0;
            if(autoClear != 0) printAutoClearItems(session,j,p,job);
            suggestedClear = j.getJobParameterByName("SuggestedClearRowCount") != null ? j.getJobParameterByName("SuggestedClearRowCount").getOutValueNumber().intValue() : 0;
            if(suggestedClear != 0) printSuggestedClearItems(session,j,p,job);
            totalCollected = j.getJobParameterByName("OUT_ROWCOUNT") != null ? j.getJobParameterByName("OUT_ROWCOUNT").getOutValueNumber().intValue() : 0;
            if(totalCollected != 0 && !collectRuleSet1) printOpenItems(session,j,p,job);
            if(j.getJobParameterByName("SuggestedClearRowCount") == null) suggestedClear = totalCollected;
        }
    }
    public DataTransformer(SchedulerSession session,Job j,boolean collectRuleSet1,PrintWriter p,Job job, String companyCode, String accountGroup, String type, DateTimeZone parentDate, boolean suggestedClearFlag)
            throws Exception
    {
        this.parentDate = parentDate;
        this.j = j;
        this.companyCode = companyCode;
        this.accountGroup = accountGroup;
        this.type = type;
        id = j.getJobId();
        p.println("DataTransformer created - " + id);
        runStartDate = j.getRunStart();
        runEndDate = j.getRunEnd();
        status = j.getStatus().getTranslationEN();
        name = j.getJobDefinition().getMasterJobDefinition().getName();
        if(!status.equals("Completed"))
        {
            ruleSet1 = 0;
            autoClear = 0;
            suggestedClear = 0;
            totalCollected = 0;
        }
        else
        {
            ruleSet1 = j.getJobParameterByName("RuleSet1RowCount") != null ? j.getJobParameterByName("RuleSet1RowCount").getOutValueNumber().intValue() : 0;
            if(collectRuleSet1) printRuleSet1Items(session,j,p,job);
            autoClear = j.getJobParameterByName("AutoClearRowCount") != null ? j.getJobParameterByName("AutoClearRowCount").getOutValueNumber().intValue() : 0;
            if(autoClear != 0) printAutoClearItems(session,j,p,job);
            suggestedClear = j.getJobParameterByName("SuggestedClearRowCount") != null ? j.getJobParameterByName("SuggestedClearRowCount").getOutValueNumber().intValue() : 0;
            if(suggestedClear != 0) printSuggestedClearItems(session,j,p,job);
            totalCollected = j.getJobParameterByName("OUT_ROWCOUNT") != null ? j.getJobParameterByName("OUT_ROWCOUNT").getOutValueNumber().intValue() : 0;
            if(totalCollected != 0 && !collectRuleSet1) printOpenItems(session,j,p,job);
            if(j.getJobParameterByName("SuggestedClearRowCount") == null) suggestedClear = totalCollected;
            if(suggestedClearFlag) printSetWithCondition(session,j,"RuleSet1",p,job,"proposed");
        }
    }
    private Set<String> getSuggestedClearItems(Job j,PrintWriter p)
            throws Exception
    {
        return getSet(j,"SuggestedClear",p);
    }

    private Set<String> getAutoClearItems(Job j,PrintWriter p)
            throws Exception
    {
        return getSet(j,"AutoClear",p);
    }

    private Set<String> getOpenItems(Job j,PrintWriter p)
            throws Exception
    {
        return getSet(j,"OUT_TABLE",p);
    }
    private Set<String> getRuleSet1Items(Job j,PrintWriter p)
            throws Exception
    {
        return getSet(j,"RuleSet1",p);
    }
    private Set<String> getSet(Job j,String parameter,PrintWriter p)
            throws Exception
    {
        String jobStatus = j.getStatus().getTranslationEN();
        if(!jobStatus.equals("Completed")) return null;
        if(j.getJobParameterByName(parameter).getOutValueTableParameter() == null) return new HashSet<>();
        Set<String>answer = new HashSet<>();
        try(RTXReader reader = j.getJobParameterByName(parameter).getOutValueTableParameter().getRTXReader() )
        {
            for(RTXRow r : reader.rows())
            {
                String belnr = r.getString("BELNR");
                String buzei = r.getString("BUZEI");
                String gjahr = r.getString("GJAHR");
                String rldnr = (r.getMetadata().hasColumn("RLDNR")) ? r.getString("RLDNR") : "";
                if(answer.contains(belnr + buzei + gjahr + rldnr))
                {
                    p.println(belnr + buzei + gjahr + rldnr + " already existst in set - skipping...");
                    skippedCount++;
                }
                answer.add(belnr + buzei + gjahr + rldnr);
            }
        }
        return answer;
    }

    private void printSuggestedClearItems(SchedulerSession session,Job j,PrintWriter p, Job job)
            throws Exception
    {
        printSet(session,j,"SuggestedClear",p,job,"proposed");
    }

    private void printAutoClearItems(SchedulerSession session,Job j,PrintWriter p, Job job)
            throws Exception
    {
        printSet(session,j,"AutoClear",p,job,"proposed");
    }

    private void printOpenItems(SchedulerSession session,Job j,PrintWriter p,Job job)
            throws Exception
    {
        printSet(session,j,"OUT_TABLE",p,job,"proposed");
    }
    private void printRuleSet1Items(SchedulerSession session,Job j,PrintWriter p,Job job)
            throws Exception
    {
        printSet(session,j,"RuleSet1",p,job,"total");
    }
    private void printSet(SchedulerSession session,Job j,String parameter,PrintWriter p, Job job, String name)
            throws Exception
    {
        String jobStatus = j.getStatus().getTranslationEN();
        if(!jobStatus.equals("Completed")) return;
        if(j.getJobParameterByName(parameter) == null) return;
        if(j.getJobParameterByName(parameter).getOutValueTableParameter() == null) return;
        String fileName = getFileName(new FileKey(parentDate,companyCode,accountGroup,type,name));
        boolean append = true;
        JobFile jf = job.getJobFileByName(fileName);
        if(jf == null)
        {
            p.println("file missing: " + fileName);
            jf = createJobFile(session,job, fileName);
            append = false;
        }
        try(FileOutputStream out = new FileOutputStream(jf.getFileName(),append))
        {
            try(RTXReader reader = j.getJobParameterByName(parameter).getOutValueTableParameter().getRTXReader() )
            {
                for(RTXRow r : reader.rows())
                {
                    String belnr = r.getString("BELNR");
                    String buzei = r.getString("BUZEI");
                    String gjahr = r.getString("GJAHR");
                    String rldnr = (r.getMetadata().hasColumn("RLDNR")) ? r.getString("RLDNR") : "";
                    String doc = belnr + buzei + gjahr + rldnr;
                    write(out,doc);
                }
            }
        }
    }
    private void printSetWithCondition(SchedulerSession session,Job j,String parameter,PrintWriter p, Job job, String name)
            throws Exception
    {
        String jobStatus = j.getStatus().getTranslationEN();
        if(!jobStatus.equals("Completed")) return;
        if(j.getJobParameterByName(parameter) == null) return;
        if(j.getJobParameterByName(parameter).getOutValueTableParameter() == null) return;
        String fileName = getFileName(new FileKey(parentDate,companyCode,accountGroup,type,name));
        boolean append = true;
        JobFile jf = job.getJobFileByName(fileName);
        if(jf == null)
        {
            p.println("file missing: " + fileName);
            jf = createJobFile(session,job, fileName);
            append = false;
        }
        try(FileOutputStream out = new FileOutputStream(jf.getFileName(),append))
        {
            try(RTXReader reader = j.getJobParameterByName(parameter).getOutValueTableParameter().getRTXReader() )
            {
                for(RTXRow r : reader.rows())
                {
                    String belnr = r.getString("BELNR");
                    String buzei = r.getString("BUZEI");
                    String gjahr = r.getString("GJAHR");
                    String rldnr = (r.getMetadata().hasColumn("RLDNR")) ? r.getString("RLDNR") : "";
                    String autoComment = (r.getMetadata().hasColumn("Auto_comment")) ? r.getCanonicalStringValue("Auto_comment") : "";
                    String doc = belnr + buzei + gjahr + rldnr;
                    if(autoComment.equals("Proposed for Clearing")) write(out,doc);
                }
            }
        }
    }
    public int getRuleSet1()
    {
        return ruleSet1;
    }
    public int getAutoClear()
    {
        return autoClear;
    }
    public int getSuggestedClear()
    {
        return suggestedClear;
    }
    public int getTotalCollected()
    {
        return totalCollected;
    }
    public Job getJob()
    {
        return j;
    }
    public Set<String> getTotalCollectedSet()
    {
        return totalCollectedSet;
    }
    public Set<String> getAutoClearSet()
    {
        return autoClearSet;
    }
    public Set<String> getSuggestedClearSet()
    {
        return suggestedClearSet;
    }
    public void clearSets()
    {
        autoClearSet = null;
        suggestedClearSet = null;
        totalCollectedSet = null;
    }
}