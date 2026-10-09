package com.redwood.scheduler.custom.kpi.kpi3.config;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.model.*;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.io.PrintWriter;

public enum HandlerType
{
    CONDITIONAL_REVIEW
            {
                @Override
                public void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob,Logger log)
                        throws Exception
                {
                    new ConditionalClearingSuggestion(topLevelJob).collectActionItems(session, p, extractorJob,log);
                }
            },
    CONDITIONAL_REQUEST
            {
                @Override
                public void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob,Logger log)
                        throws Exception
                {
                    new ConditionalClearingRequest(topLevelJob).collectChildren(session, p, extractorJob,log);
                }
            },
    AUTO
            {
                @Override
                public void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob,Logger log)
                        throws Exception
                {
                    new AutoClearing(topLevelJob).collectActionItems(session, p, extractorJob,log);
                }
            },
    RECON
            {
                @Override
                public void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob,Logger log)
                        throws Exception
                {
                    new Recon(topLevelJob).collectActionItems(session, p, extractorJob);
                }
            },
    SUGGESTED
            {
                @Override
                public void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob, Logger log)
                        throws Exception
                {
                    new SuggestedClearing(topLevelJob).collectActionItems(session, p, extractorJob,log);
                }
            };
    public abstract void handle(Job topLevelJob, SchedulerSession session, PrintWriter p, Job extractorJob,Logger log)
            throws Exception;
}
