package com.redwood.scheduler.custom.kpi.kpi3.config;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.model.*;

import java.io.PrintWriter;

public enum HandlerType
{
    CONDITIONAL_REVIEW
            {
                @Override
                public void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
                        throws Exception
                {
                    new ConditionalClearingSuggestion(j).collectActionItems(session, p, parent);
                }
            },
    CONDITIONAL_REQUEST
            {
                @Override
                public void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
                        throws Exception
                {
                    new ConditionalClearingRequest(j).collectChildren(session, p, parent);
                }
            },
    AUTO
            {
                @Override
                public void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
                        throws Exception
                {
                    new AutoClearing(j).collectActionItems(session, p, parent);
                }
            },
    RECON
            {
                @Override
                public void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
                        throws Exception
                {
                    new Recon(j).collectActionItems(session, p, parent);
                }
            },
    SUGGESTED
            {
                @Override
                public void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
                        throws Exception
                {
                    new SuggestedClearing(j).collectActionItems(session, p, parent);
                }
            };
    public abstract void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
            throws Exception;
}
