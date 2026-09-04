package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;


import java.util.List;
import java.util.Set;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec.getFileName;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getAccountGroups;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

class AccountItemSuggested
{
    private DateTimeZone runStartDate;
    private DateTimeZone runEndDate;
    private DateTimeZone parentDate;
    private String status;
    private String name;
    private Long id;
    private String accountGroup;
    private String companyCode;
    private Long clearId = -1l;
    private Long prepId;
    private int totalOpenItems = 0;
    private Set<String> totalOpenItemsSet;
    private int itemsCleared = 0;
    private Set<String> itemsClearedSet;
    private int ruleSet1 = 0;
    private int autoClear = 0;
    private int errors = 0;
    private Set<String> errorsSet;
    private int suggestedClear = 0;
    private int totalCollected = 0;
    private Set<String> totalCollectedSet;
    //private List<DataTransformer> dts;
    private Job j;

    public AccountItemSuggested(Job j, DateTimeZone parentDate)
            throws Exception
    {
        this.parentDate = parentDate;
        this.j = j;
        id = j.getJobId();
        runStartDate = j.getRunStart();
        runEndDate = j.getRunEnd();
        status = j.getStatus().getTranslationEN();
        name = j.getJobDefinition().getName();
        accountGroup = getAccountGroups(j);
        companyCode = getParameter(j,"BUKRS");
    }

    private void addDt(DataTransformer dt)
    {
        if (ruleSet1 == 0) ruleSet1 = dt.getRuleSet1();
        autoClear += dt.getAutoClear();
        suggestedClear += dt.getSuggestedClear();
        totalCollected += autoClear + suggestedClear;
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
    public int getItemsCleared()
    {
        return itemsCleared;
    }
    public int getTotalOpenItems()
    {
        return totalOpenItems;
    }
    public int getTotalCollected()
    {
        return totalCollected;
    }
    public int getErrors()
    {
        return errors;
    }
    public void collectChildren(SchedulerSession session,PrintWriter p, Job job)
            throws Exception
    {
        p.println("JOBSTATUS: " + status);
        if(!"Waiting".equals(status) && !"Completed".equals(status)) return;
        scan(session,j,p,job);
    }
    private void scan(SchedulerSession session,Job parent,PrintWriter pw,Job job)
            throws Exception
    {
        List<String> clearingParents = List.of("CUS_TD_BSC_SuggestedClearing_OneSided_MASTER_SHERPAX","CUS_TD_BSC_SuggestedClearing_OneSided_MASTER");
        List<String> collectParents = List.of("CUS_TD_BSC_SuggestedClearing_OneSided_MASTER_SHERPAX","CUS_TD_BSC_SuggestedClearing_OneSided_MASTER");
        List<String> baseParents = List.of("CUS_TD_BSC_SuggestedClearing_OneSided_MASTER_SHERPAX","CUS_TD_BSC_SuggestedClearing_OneSided_MASTER");
        List<String> dtParents = List.of("CUS_SPD_BSC_RULES_OIMGL_SHERPAX","CUS_SPD_BSC_SUGGESTEDCLEAR_RULES_WEA_new");
        String p = parent.getJobDefinition().getName();
        for(Job child: parent.getChildJobs())
        {
            String c = child.getJobDefinition().getName();
            //pw.println(p + "_______" + c);
      /*
      if(baseParents.contains(p)&&(c).contains("BaseWorking"))
      {
        pw.println("Creating baseworking DT");
        DataTransformer tot = new DataTransformer(session,child,true,pw,job,companyCode,accountGroup,"suggested",parentDate);
        totalOpenItems = tot.getRuleSet1();
        //totalOpenItemsSet = tot.getTotalCollectedSet();
        tot = null;
      }
      else if(dtParents.contains(p) && "lastrule".equalsIgnoreCase(child.getJobChainStep().getName()) && c.startsWith("CUS_DT"))
      {
        pw.println("Creating LastRule DT");
        DataTransformer dt = new DataTransformer(session,child,false,pw,job,companyCode,accountGroup,"suggested",parentDate,true);
        totalCollected += dt.getTotalCollected();
        dt = null;
      }
      else if(dtParents.contains(p) && c.startsWith("CUS_DT"))
      {
        pw.println("Creating standard DT");
        DataTransformer dt = new DataTransformer(session,child,false,pw,job,companyCode,accountGroup,"suggested",parentDate);
        addDt(dt);
        dt = null;
      }
      else
      */
            if(baseParents.contains(p) && c.contains("Certification_Process_Account"))
            {
                String certId = getParameter(child,"CERT_UNIQUE_ID");
                if(certId != null)
                {
                    pw.println("Cert ID: " + getParameter(child,"CERT_UNIQUE_ID"));
                    String fileName = getFileName(new FileKey(parentDate,companyCode,accountGroup,"suggested","certId"));//errors / cleared
                    boolean append = true;
                    JobFile jf = job.getJobFileByName(fileName);
                    if(jf == null)
                    {
                        pw.println("file missing: " + fileName);
                        jf = createJobFile(session,job, fileName);
                        append = false;
                    }
                    try(FileOutputStream out = new FileOutputStream(jf.getFileName(),append))
                    {
                        write(out,certId);
                    }

                }
            }

            scan(session,child,pw,job);
        }

    }

    public void clearSets()
    {
        totalOpenItemsSet = null;
        itemsClearedSet = null;
        errorsSet = null;
        totalCollectedSet = null;
    }
    public Set<String> getTotalOpenItemsSet()
    {
        return totalOpenItemsSet;
    }
    public Set<String> getTotalCollectedSet()
    {
        return totalCollectedSet;
    }
    public Set<String> getItemsClearedSet()
    {
        return itemsClearedSet;
    }
    public Set<String> getErrorsSet()
    {
        return errorsSet;
    }

    public DateTimeZone getRunStartDate()
    {
        return runStartDate;
    }
    public DateTimeZone getRunEndDate()
    {
        return runEndDate;
    }
    public String getStatus()
    {
        return status;
    }
    public String getName()
    {
        return name;
    }
    public Long getId()
    {
        return id;
    }
    public Long getClearId()
    {
        return clearId;
    }
    public Long getPrepId()
    {
        return prepId;
    }
    public String getAccountGroup()
    {
        return accountGroup;
    }
    public String getCompanyCode()
    {
        return companyCode;
    }
}