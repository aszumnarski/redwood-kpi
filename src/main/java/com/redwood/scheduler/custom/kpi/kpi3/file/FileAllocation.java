package com.redwood.scheduler.custom.kpi.kpi3.file;

import java.util.ArrayList;
import java.util.List;

public class FileAllocation
{
    private final List<String> conditional = new ArrayList<>();
    private final List<String> total = new ArrayList<>();
    private final List<String> proposed = new ArrayList<>();
    private final List<String> selected = new ArrayList<>();
    private final List<String> cleared = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final List<String> missings = new ArrayList<>();
    private final List<String> certId = new ArrayList<>();

    public void add(String path)
    {
        if (path.endsWith("total.csv"))
        {
            total.add(path);
        }
        else if (path.endsWith("proposed.csv"))
        {
            proposed.add(path);
        }
        else if (path.endsWith("selected.csv"))
        {
            selected.add(path);
        }
        else if (path.endsWith("cleared.csv"))
        {
            cleared.add(path);
        }
        else if (path.endsWith("errors.csv"))
        {
            errors.add(path);
        }
        else if (path.endsWith("missing_chains.csv"))
        {
            missings.add(path);
        }
        else if (path.endsWith("certId.csv"))
        {
            certId.add(path);
        }
        else if (path.endsWith("conditional_summary.csv"))
        {
            conditional.add(path);
        }
    }

    public boolean hasAnyKpiFile()
    {
        return !(conditional.isEmpty()
                && total.isEmpty()
                && proposed.isEmpty()
                && selected.isEmpty()
                && cleared.isEmpty()
                && errors.isEmpty()
                && certId.isEmpty());
    }

    public List<String> getConditional()
    {
        return conditional;
    }

    public List<String> getTotal()
    {
        return total;
    }

    public List<String> getProposed()
    {
        return proposed;
    }

    public List<String> getSelected()
    {
        return selected;
    }

    public List<String> getCleared()
    {
        return cleared;
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public List<String> getMissings()
    {
        return missings;
    }

    public List<String> getCertId()
    {
        return certId;
    }

    @Override
    public String toString()
    {
        return "FileAllocation{" +
                "totalFiles=" + total +
                ", proposedFiles=" + proposed +
                ", selectedFiles=" + selected +
                ", clearedFiles=" + cleared +
                ", errorFiles=" + errors +
                ", missings=" + missings +
                ", conditional=" + conditional +
                ", certId=" + certId +
                '}';
    }
}