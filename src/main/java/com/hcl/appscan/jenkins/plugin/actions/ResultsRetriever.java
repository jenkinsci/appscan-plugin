/**
 * © Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2019, 2020.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.actions;

import com.hcl.appscan.jenkins.plugin.util.ExecutorUtil;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.scanners.ScanConstants;
import com.hcl.appscan.sdk.utils.ServiceUtil;
import hudson.model.Action;
import hudson.model.Run;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep;

import org.kohsuke.stapler.DataBoundConstructor;

import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.jenkins.plugin.Messages;

public class ResultsRetriever extends AppScanAction implements RunAction2, SimpleBuildStep.LastBuildAction {

	private final Run<?,?> m_build;	
	private IResultsProvider m_provider;
	private String m_name;
	private String m_status;
	private String m_message;
	private String m_scanServerUrl;
	private String m_label;
	transient private Future<Boolean> futureTask = null;
	private Boolean m_resultsAvailable;

	@DataBoundConstructor
	public ResultsRetriever(Run<?,?> build, IResultsProvider provider, String scanName, String scanServerUrl, String label) {
		super(build.getParent());
		m_build = build;
		m_provider = provider;
		m_name = scanName;
		m_resultsAvailable = false;
		m_scanServerUrl = scanServerUrl;
		m_label = label;
	}

	@Override
	public String getDisplayName() {
		return Messages.label_running(m_name);
	}

	@Override
	public String getUrlName() {
		return null;
	}

	@Override
	public void onAttached(Run<?, ?> r) {
	}

	@Override
	public void onLoad(Run<?, ?> r) {
		checkResults(r);
	}
	
	@Override
	public Collection<? extends Action> getProjectActions() {
		HashSet<Action> actions = new HashSet<Action>();
		actions.add(new ScanResultsTrend(m_build, m_provider.getType(), m_name));
		return actions;
	}
	
	public boolean getHasResults() {
		return checkResults(m_build);
	}

	public boolean getFailed() {
		return CoreConstants.FAILED.equalsIgnoreCase(m_status);
	}

	public String getMessage() {
		return m_message;
	}

	public String getScanType() {
		if (m_provider == null) return "";
		return m_provider.getType();
	}

	public boolean checkResults(Run<?,?> r) {
		boolean results = false;
		if (m_resultsAvailable != null && m_resultsAvailable) return true;
		if (futureTask != null && futureTask.isDone()) {
			try {
				results = futureTask.get();
				m_resultsAvailable = results;
			} catch (Exception e) {
			}
		} else if (futureTask != null) return false;

		if (!results) {
			final Run<?,?> rTemp  = r;
			Callable<Boolean> callableTask = new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					String status = null;
					if (rTemp.getAllActions().contains(ResultsRetriever.this) && CoreConstants.FAILED.equalsIgnoreCase(status = m_provider.getStatus())) {
						String message = com.hcl.appscan.sdk.Messages.getMessage(ScanConstants.SCAN_FAILED, " Scan Name: " + m_name);
						if (m_provider.getMessage() != null  && m_provider.getMessage().trim().length() > 0) message += ", " + m_provider.getMessage();

						ResultsRetriever.this.m_status = status;
						ResultsRetriever.this.m_message = message;
						return true;
					} else if (rTemp.getAllActions().contains(ResultsRetriever.this) && m_provider.hasResults()) {
						rTemp.getActions().remove(ResultsRetriever.this); //We need to remove this action from the build, but getAllActions() returns a read-only list.
						if(m_provider.getResultProvider1() !=null || m_provider.getResultProvider2() !=null) {
							createCombinedBuildSummary(rTemp);
						} else {
							rTemp.addAction(createResults());
						}
						try {
							rTemp.save();
						} catch (IOException e) {
						}

						ResultsRetriever.this.m_status = status;
						return true;
					}

					if (m_provider.getMessage() != null) {
					    ResultsRetriever.this.m_message = m_provider.getMessage();
					}

					return false;
				}
			};
			futureTask = (Future<Boolean>) ExecutorUtil.submitTask(callableTask);
		}

		return results;
	}

    private void createCombinedBuildSummary(Run<?,?> rTemp) {
            if (m_provider.getResultProvider1() != null && m_provider.getResultProvider1().getStatus().equals(CoreConstants.READY)) {
                rTemp.addAction(new ScanResults(m_build, m_provider.getResultProvider1(),ServiceUtil.scanTypeShortForm(m_provider.getResultProvider1().getType()).toUpperCase()+"_"+m_name, m_provider.getResultProvider1().getStatus(), m_provider.getResultProvider1().getFindingsCount(), m_provider.getResultProvider1().getCriticalCount(), m_provider.getResultProvider1().getHighCount(), m_provider.getResultProvider1().getMediumCount(), m_provider.getResultProvider1().getLowCount(), m_provider.getResultProvider1().getInfoCount(), m_scanServerUrl, m_label));
            }
            if (m_provider.getResultProvider2() != null && m_provider.getResultProvider2().getStatus().equals(CoreConstants.READY)) {
                rTemp.addAction(new ScanResults(m_build, m_provider.getResultProvider2(),ServiceUtil.scanTypeShortForm(m_provider.getResultProvider2().getType()).toUpperCase()+"_"+m_name, m_provider.getResultProvider2().getStatus(), m_provider.getResultProvider2().getFindingsCount(), m_provider.getResultProvider2().getCriticalCount(), m_provider.getResultProvider2().getHighCount(), m_provider.getResultProvider2().getMediumCount(), m_provider.getResultProvider2().getLowCount(), m_provider.getResultProvider2().getInfoCount(), m_scanServerUrl, m_label));
            }
    }

	private ScanResults createResults() {
		return new ScanResults(
				m_build,
				m_provider,
				m_name,
				m_provider.getStatus(),
				m_provider.getFindingsCount(),
                		m_provider.getCriticalCount(),
                		m_provider.getHighCount(),
				m_provider.getMediumCount(),
				m_provider.getLowCount(),
				m_provider.getInfoCount(),
				m_scanServerUrl,
				m_label);
	}
}
