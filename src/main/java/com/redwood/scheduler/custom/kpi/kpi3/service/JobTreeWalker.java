package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.model.Job;

public class JobTreeWalker {

    public enum WalkResult {
        CONTINUE,
        SKIP_SUBTREE,
        STOP
    }

    @FunctionalInterface
    public interface JobVisitor {
        WalkResult visit(Job parent, Job child) throws Exception;
    }

    public static void walk(Job root, JobVisitor visitor) throws Exception {
        walkRecursive(null, root, visitor);
    }

    private static WalkResult walkRecursive(Job parent, Job current, JobVisitor visitor) throws Exception {

        if (parent != null) {
            WalkResult result = visitor.visit(parent, current);

            if (result == WalkResult.STOP) return WalkResult.STOP;
            if (result == WalkResult.SKIP_SUBTREE) return WalkResult.CONTINUE;

        }

        for (Job child : current.getChildJobs()) {
            WalkResult result = walkRecursive(current, child, visitor);

            if (result == WalkResult.STOP) return WalkResult.STOP;

        }

        return WalkResult.CONTINUE;
    }
}
