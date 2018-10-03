/**
 * © Copyright IBM Corporation 2016.
 * © Copyright HCL Technologies Ltd. 2017,2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.results;

import java.util.List;

import com.hcl.appscan.sdk.results.IResultsProvider;

public class ResultsInspector {

	private List<FailureCondition> m_conditions;
	private IResultsProvider m_resultsProvider;
	
	public ResultsInspector(List<FailureCondition> conditions, IResultsProvider resultsProvider) {
		m_conditions = conditions;
		m_resultsProvider = resultsProvider;
	}
        
        
	public boolean shouldFail() {
		for(FailureCondition condition : m_conditions) {
			String type = condition.getFailureType();
			int threshold = condition.getThreshold();
			if(exceedsThreshold(type, threshold))
				return true;
		}
		return false;
	}
        

	private boolean exceedsThreshold(String type, int threshold) {
		switch(type.toLowerCase()) {
		case "total": //$NON-NLS-1$
			return m_resultsProvider.getFindingsCount() > threshold;
		case "high": //$NON-NLS-1$
			return m_resultsProvider.getHighCount() > threshold;
		case "medium": //$NON-NLS-1$
			return m_resultsProvider.getMediumCount() > threshold;
		case "low": //$NON-NLS-1$
			return m_resultsProvider.getLowCount() > threshold;
		default:
			return false;
		}
	}
}
