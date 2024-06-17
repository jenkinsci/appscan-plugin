/**
 * © Copyright IBM Corporation 2016.
 * © Copyright HCL Technologies Ltd. 2017,2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.results;

import java.util.List;
import java.util.Map;

import com.hcl.appscan.jenkins.plugin.scanners.Scanner;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.results.IResultsProvider;

public class ResultsInspector {

	private List<FailureCondition> m_conditions;
	private IResultsProvider m_resultsProvider;
    public int totalCountSAST;
    private int criticalCountSAST;
    private int highCountSAST;
    private int mediumCountSAST;
    private int lowCountSAST;
	
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

    public boolean shouldFailCombined(Map<String,String> properties) {
        for(FailureCondition condition : m_conditions) {
            String type = condition.getFailureType();
            int threshold = condition.getThreshold();
            if(exceedsThresholdCombined(type, threshold, properties))
                return true;
        }
        return false;
    }
        

	private boolean exceedsThreshold(String type, int threshold) {
		switch(type.toLowerCase()) {
		case "total": //$NON-NLS-1$
			return m_resultsProvider.getFindingsCount() > threshold;
                case "critical": //$NON-NLS-1$
                        return m_resultsProvider.getCriticalCount() > threshold;
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

    private void issuesCountSAST() {
        totalCountSAST = m_resultsProvider.getFindingsCount();
        criticalCountSAST = m_resultsProvider.getCriticalCount();
        highCountSAST = m_resultsProvider.getHighCount();
        mediumCountSAST = m_resultsProvider.getMediumCount();
        lowCountSAST = m_resultsProvider.getLowCount();
    }

    private boolean exceedsThresholdCombined(String type, int threshold, Map<String,String> properties) {
        String scanType = properties.get(CoreConstants.SCANNER_TYPE);
        if (scanType.equals(Scanner.STATIC_ANALYZER)) {
            issuesCountSAST();
        } else {
            switch(type.toLowerCase()) {
                case "total": //$NON-NLS-1$
                    return (totalCountSAST+m_resultsProvider.getFindingsCount()) > threshold;
                case "critical": //$NON-NLS-1$
                    return criticalCountSAST+m_resultsProvider.getCriticalCount() > threshold;
                case "high": //$NON-NLS-1$
                    return highCountSAST+m_resultsProvider.getHighCount() > threshold;
                case "medium": //$NON-NLS-1$
                    return mediumCountSAST+m_resultsProvider.getMediumCount() > threshold;
                case "low": //$NON-NLS-1$
                    return lowCountSAST+m_resultsProvider.getLowCount() > threshold;
                default:
                    return false;
            }
        }
        return false;
    }
}
