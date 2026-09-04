package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.rtx.RTXReader;
import com.redwood.scheduler.api.rtx.RTXRow;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec.getFileName;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getAccountGroups;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

public class ConditionalClearingRequest
{
    private DateTimeZone runStartDate;
    private DateTimeZone runEndDate;
    private String status;
    private String name;
    private Long suggestionId;
    private Long id;
    private String accountGroups;
    private String companyCode;
    private Long excelId = -1l;
    private Long prepId = -1l;
    private Long clearId = -1l;
    private int totalOpenItems = 0;
    private int selectedForClear = 0;
    private int itemsCleared = 0;
    private int errors = 0;
    private int totalCollected = 0;
    private Job j;
    private int skippedCount = 0;

    public ConditionalClearingRequest(Job j)
            throws Exception
    {
        this.j = j;
        id = j.getJobId();
        runStartDate = j.getRunStart();
        runEndDate = j.getRunEnd();
        status = j.getStatus().getTranslationEN();
        name = j.getJobDefinition().getName();
        suggestionId = getLink(j);
        accountGroups = getAccountGroups(j);
        companyCode = getParameter(j,"BUKRS");
    }
    public void collectChildren(SchedulerSession session,PrintWriter p, Job job)
            throws Exception
    {
        scan(session,j,p,job);
    }
    private void scan(SchedulerSession session, Job parent,PrintWriter p, Job job)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {
            String name = child.getJobDefinition().getName();
            switch(name)
            {
                case("CUS_ConvertExcel2CSV2RTX_NoReplaceAll"):
                    if(excelId == -1l)
                    {
                        excelId = child.getJobId();
                    }
                    else
                    {
                        throw new Exception("Excel file 2x in chain " + child.getJobId());
                    }
                    String excelStatus = child.getStatus().getTranslationEN();
                    String outLines = getParameter(child,"OUT_LINES");
                    if(excelStatus.equals("Completed"))
                    {
                        totalOpenItems = Integer.parseInt(outLines);
                        if(totalOpenItems > 0) printTotalOpenItemsSet(session,child,p,job);
                    }
                    break;
                case("CUS_DT_BSC_CONDITIONAL_AUTOCLEAR_Clearing"):
                    if(prepId == -1l)
                    {
                        prepId = child.getJobId();
                    }
                    else
                    {
                        p.println("Prep dt file 2x in chain " + child.getJobId());
                    }
                    String selectedStatus = child.getStatus().getTranslationEN();
                    String prepClearing = getParameter(child,"PrepClearingRowCount");
                    if(selectedStatus.equals("Completed"))
                    {
                        selectedForClear = Integer.parseInt(prepClearing);
                        if(selectedForClear > 0) printSelectedForClearSet(session,child,p,job);
                    }
                    break;
                case("FCA_SAP_Tran_FB05_Clearing"):
                    if(clearId == -1l)
                    {
                        clearId = child.getJobId();
                    }
                    else
                    {
                        p.println("Clear 2x in chain " + child.getJobId());
                    }
                    String status = child.getStatus().getTranslationEN();
                    String okLines = getParameter(child,"OUT_DATA_OK_RTX");
                    String errorLines = getParameter(child,"OUT_DATA_ERROR_RTX");
                    Map<String,List<String>> matchings = getMatchingRtx(child);
                    if(errorLines == null)
                    {
                        itemsCleared = selectedForClear; //all records cleared w/o errors
                        if(matchings.size() > 0) writeItemsToFile(session, "cleared", matchings, job,p);

                    }
                    else if(errorLines != null && okLines == null)
                    {
                        errors = selectedForClear; // no items cleared
                        if(matchings.size() > 0) writeItemsToFile(session, "errors", matchings, job,p);
                    }
                    else
                    {
                        if(matchings.size()>0) writeErrorsAndClearedSeparately(session,child,matchings,job,p);
                    }
                    break;
                default:
                    scan(session,child,p,job);
                    break;
            }
        }
    }

    private void writeItemsToFile(SchedulerSession session,String type, Map<String, List<String>> itemsMap, Job job,PrintWriter p)
            throws Exception
    {
        p.println(itemsMap);
        String fileName = getFileName(new FileKey(runStartDate,companyCode,accountGroups,"conditional",type));
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
                p.println(key);
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
    private void writeErrorsAndClearedSeparately(SchedulerSession session,Job child, Map<String, List<String>> matchings,Job job,PrintWriter p)
            throws Exception
    {
        p.println(matchings);
        List<String> errorsList = getErrorsRtx(child);
        p.println(errorsList);
        Map<String, List<String>> errorsMap = new HashMap<>();
        Map<String, List<String>> clearedMap = new HashMap<>(matchings);
        for (String error : errorsList)
        {
            errorsMap.put(error, matchings.get(error));
            clearedMap.remove(error);
        }
        if(errorsMap.size() > 0) writeItemsToFile(session,"errors", errorsMap,job,p);
        if(clearedMap.size() > 0) writeItemsToFile(session,"cleared", clearedMap,job,p);
    }

    private Set<String> getTotalOpenItemsSet(Job j,PrintWriter p)
            throws Exception
    {
        return getSet(j,"OUT_RTX",p);
    }

    private void printTotalOpenItemsSet(SchedulerSession session,Job j,PrintWriter p, Job job)
            throws Exception
    {
        printSet(session,j,"OUT_RTX",p,job,"total");
    }

    private Set<String> getSelectedForClearSet(Job j,PrintWriter p)
            throws Exception
    {
        return getSet(j,"PrepClearing",p);
    }

    private void printSelectedForClearSet(SchedulerSession session,Job j,PrintWriter p, Job job)
            throws Exception
    {
        printSet(session,j,"PrepClearing",p,job,"proposed");
    }

    private Set<String> getSet(Job j,String parameter,PrintWriter p)
            throws Exception
    {
        String jobStatus = j.getStatus().getTranslationEN();
        if(!jobStatus.equals("Completed")) return new HashSet<>();
        if(j.getJobParameterByName(parameter).getOutValueTableParameter() == null) return new HashSet<>();
        Set<String>answer = new HashSet<>();
        try(RTXReader reader = j.getJobParameterByName(parameter).getOutValueTableParameter().getRTXReader() )
        {
            for(RTXRow r : reader.rows())
            {
                String belnr = r.getString("DocumentNo");
                String buzei = r.getString("Doc.Item");
                String gjahr = r.getString("FiscalYear");
                if(answer.contains(belnr + buzei + gjahr))
                {
                    p.println(belnr + buzei + gjahr + " already existst in set - skipping...");
                    skippedCount++;
                }
                answer.add(belnr + buzei + gjahr);
            }
        }
        return answer;
    }
    private void printSet(SchedulerSession session,Job j,String parameter,PrintWriter p,Job job,String name)
            throws Exception
    {
        String jobStatus = j.getStatus().getTranslationEN();
        if(!jobStatus.equals("Completed")) return;
        if(j.getJobParameterByName(parameter).getOutValueTableParameter() == null) return;
        try(RTXReader reader = j.getJobParameterByName(parameter).getOutValueTableParameter().getRTXReader() )
        {
            String fileName = getFileName(new FileKey(runStartDate,companyCode,accountGroups,"conditional",name));
            boolean append = true;
            JobFile jf = job.getJobFileByName(fileName);
            if(jf == null)
            {
                jf = createJobFile(session,job, fileName);
                append = false;
            }
            try(FileOutputStream out = new FileOutputStream(jf.getFileName(),append))
            {
                for(RTXRow r : reader.rows())
                {
                    String belnr = r.getString("DocumentNo");
                    String buzei = r.getString("Doc.Item");
                    String gjahr = r.getString("FiscalYear");
                    String doc = belnr + buzei + gjahr;
                    write(out,doc);
                }
            }
        }
    }

    private Map<String,List<String>> getMatchingRtx(Job j)
            throws Exception
    {
        if(j.getJobParameterByName("IN_DATA_RTX").getInValueTableParameter() == null) return new HashMap<>();
        RTXReader reader = j.getJobParameterByName("IN_DATA_RTX").getInValueTableParameter().getRTXReader();
        Map<String,List<String>> answer = new HashMap<>();
        for(RTXRow r : reader.rows())
        {
            String belnr = r.getString("DocumentNo");
            String buzei = r.getString("Doc.Item");
            String key = r.getString("Rule_MatchKey");
            List<String> list = new ArrayList<>();
            if(answer.containsKey(key)) list = answer.get(key);
            list.add(belnr + buzei);
            answer.put(key,list);
        }
        return answer;
    }

    private List<String> getErrorsRtx(Job j)
            throws Exception
    {
        if(j.getJobParameterByName("OUT_DATA_ERROR_RTX").getOutValueTableParameter() == null) return new ArrayList<>();
        RTXReader reader = j.getJobParameterByName("OUT_DATA_ERROR_RTX").getOutValueTableParameter().getRTXReader();
        List<String> answer = new ArrayList<>();
        for(RTXRow r : reader.rows())
        {
            String key = r.getString("Rule_MatchKey");
            answer.add(key);
        }
        return answer;
    }

    private Long getLink(Job j)
    {
        String inExcel = getParameter(j, "IN_FILE_FROM_EP");

        Long answer = -1L;

        if (inExcel == null)
        {
            throw new RuntimeException(
                    "Missing IN_FILE_FROM_EP parameter for job "
                            + j.getJobId()
                            + " (" + j.getJobDefinition().getName() + ")"
            );
        }



        String[] parts = inExcel.split("_", -1);

        for (String part : parts)
        {
            if (part.startsWith("ProcessID"))
            {
                answer = Long.valueOf(
                        part.replace("ProcessID", "")
                );
                break;
            }
        }

        return answer;
    }
}