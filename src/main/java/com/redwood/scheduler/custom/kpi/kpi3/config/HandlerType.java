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
                public void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob)
                        throws Exception
                {
                    new ConditionalClearingSuggestion(topLevelJob).collectActionItems(session, p, extractorJob);
                }
            },
    CONDITIONAL_REQUEST
            {
                @Override
                public void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob)
                        throws Exception
                {
                    new ConditionalClearingRequest(topLevelJob).collectChildren(session, p, extractorJob);
                }
            },
    AUTO
            {
                @Override
                public void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob)
                        throws Exception
                {
                    new AutoClearing(topLevelJob).collectActionItems(session, p, extractorJob);
                }
            },
    RECON
            {
                @Override
                public void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob)
                        throws Exception
                {
                    new Recon(topLevelJob).collectActionItems(session, p, extractorJob);
                }
            },
    SUGGESTED
            {
                @Override
                public void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob)
                        throws Exception
                {
                    new SuggestedClearing(topLevelJob).collectActionItems(session, p, extractorJob);
                }
            };
    public abstract void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob)
            throws Exception;
}
