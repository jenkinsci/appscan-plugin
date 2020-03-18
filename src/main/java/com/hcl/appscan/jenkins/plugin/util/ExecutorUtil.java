package com.hcl.appscan.jenkins.plugin.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorUtil {
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static Future submitTask(Callable task) {
        return executorService.submit(task);
    }
}
