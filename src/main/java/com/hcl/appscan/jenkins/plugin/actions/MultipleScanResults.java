/**
 * © Copyright HCL Technologies Ltd. 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.actions;

import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.sdk.utils.ServiceUtil;
import hudson.model.Run;

public class MultipleScanResults {
    private IResultsProvider m_provider1;
    private IResultsProvider m_provider2;

    public MultipleScanResults(IResultsProvider provider1, IResultsProvider provider2) {
        m_provider1 = provider1;
        m_provider2 = provider2;
    }

    public void createMultipleResults(Run<?,?> rTemp, Run<?, ?> build, String name, String serverUrl, String label) {
        if(m_provider1.hasResults()) {
            rTemp.addAction(new ScanResults(build, m_provider1, ServiceUtil.scanTypeShortForm(m_provider1.getType()).toUpperCase()+"_"+name, serverUrl+ServiceUtil.scanTypeShortForm(m_provider1.getType()).toLowerCase(), label));
        }
        if(m_provider2.hasResults()) {
            rTemp.addAction(new ScanResults(build, m_provider2, ServiceUtil.scanTypeShortForm(m_provider2.getType()).toUpperCase()+"_"+name, serverUrl+ServiceUtil.scanTypeShortForm(m_provider2.getType()).toLowerCase(), label));
        }
    }
}
