/**
 * © Copyright HCL Technologies Ltd. 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.actions;

import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.sdk.results.CloudCombinedResultsProvider;
import com.hcl.appscan.sdk.utils.ServiceUtil;
import hudson.model.Run;

public class ScanResultsFactory {

    public static void createResult(Run<?,?> rTemp, Run<?,?> build, IResultsProvider resultsProvider, String name, String serverURL, String label) {
        if(resultsProvider instanceof CloudCombinedResultsProvider) {
            CloudCombinedResultsProvider provider = (CloudCombinedResultsProvider) resultsProvider;
            new MultipleScanResults(provider.getResultsProvider1(), provider.getResultsProvider2()).createMultipleResults(rTemp, build, name, serverURL, label);
        } else {
            rTemp.addAction(new ScanResults(build, resultsProvider, name, serverURL+ServiceUtil.scanTypeShortForm(resultsProvider.getType()).toLowerCase(), label));
        }
    }

}
