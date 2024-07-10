package com.hcl.appscan.jenkins.plugin.results;

import com.hcl.appscan.jenkins.plugin.scanners.Scanner;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.results.IResultsProvider;

import java.util.List;
import java.util.Map;

public class CombinedResultsInspector {

    private List<FailureCondition> m_conditions;
    private IResultsProvider m_resultsProvider;

    public CombinedResultsInspector(List<FailureCondition> conditions, IResultsProvider resultsProvider) {
        m_conditions = conditions;
        m_resultsProvider = resultsProvider;
    }

    public boolean shouldFail(Map<String,String> properties) {
        for(FailureCondition condition : m_conditions) {
            String type = condition.getFailureType();
            int threshold = condition.getThreshold();
            if(exceedsThresholdCombined(type, threshold, properties))
                return true;
        }
        return false;
    }

    private void issuesCountSAST(Map<String,String> properties) {
        properties.put("totalCountSAST", String.valueOf(m_resultsProvider.getFindingsCount()));
        properties.put("criticalCountSAST", String.valueOf(m_resultsProvider.getCriticalCount()));
        properties.put("highCountSAST", String.valueOf(m_resultsProvider.getHighCount()));
        properties.put("mediumCountSAST", String.valueOf(m_resultsProvider.getMediumCount()));
        properties.put("lowCountSAST", String.valueOf(m_resultsProvider.getLowCount()));
    }

    private boolean exceedsThresholdCombined(String type, int threshold, Map<String,String> properties) {
        String scanType = properties.get(CoreConstants.SCANNER_TYPE);
        if (scanType.equals(Scanner.STATIC_ANALYZER)) {
            issuesCountSAST(properties);
        } else {
            switch(type.toLowerCase()) {
                case "total": //$NON-NLS-1$
                    if(properties.containsKey("totalCountSAST")) {
                        int totalCountSAST = Integer.parseInt(properties.remove("totalCountSAST"));
                        return (totalCountSAST+m_resultsProvider.getFindingsCount()) > threshold;
                    }
                    return m_resultsProvider.getFindingsCount() > threshold;
                case "critical": //$NON-NLS-1$
                    if(properties.containsKey("criticalCountSAST")) {
                        int criticalCountSAST = Integer.parseInt(properties.remove("criticalCountSAST"));
                        return (criticalCountSAST+m_resultsProvider.getCriticalCount()) > threshold;
                    }
                    return m_resultsProvider.getCriticalCount() > threshold;
                case "high": //$NON-NLS-1$
                    if(properties.containsKey("highCountSAST")) {
                        int highCountSAST = Integer.parseInt(properties.remove("highCountSAST"));
                        return (highCountSAST+m_resultsProvider.getHighCount()) > threshold;
                    }
                    return m_resultsProvider.getHighCount() > threshold;
                case "medium": //$NON-NLS-1$
                    if(properties.containsKey("mediumCountSAST")) {
                        int mediumCountSAST = Integer.parseInt(properties.remove("mediumCountSAST"));
                        return (mediumCountSAST+m_resultsProvider.getMediumCount()) > threshold;
                    }
                    return m_resultsProvider.getMediumCount() > threshold;
                case "low": //$NON-NLS-1$
                    if(properties.containsKey("lowCountSAST")) {
                        int lowCountSAST = Integer.parseInt(properties.remove("lowCountSAST"));
                        return (lowCountSAST+m_resultsProvider.getLowCount()) > threshold;
                    }
                    return m_resultsProvider.getLowCount() > threshold;
                default:
                    return false;
            }
        }
        return false;
    }
}
