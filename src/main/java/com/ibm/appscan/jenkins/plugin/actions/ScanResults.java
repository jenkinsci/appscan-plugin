/* 
 * @ Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.actions;

import com.hcl.appscan.sdk.results.IResultsProvider;

import hudson.model.Run;

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class ScanResults extends com.hcl.appscan.jenkins.plugin.actions.ScanResults {

	public ScanResults(Run<?, ?> build, IResultsProvider provider, String name, String status, int totalFindings, int criticalCount,
			int highCount, int mediumCount, int lowCount, int infoCount) {
		super(build, provider, name, status, totalFindings, criticalCount, highCount, mediumCount, lowCount, infoCount, null, null);
	}
}