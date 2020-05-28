/**
 * © Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2019, 2020.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.actions;

import com.hcl.appscan.jenkins.plugin.util.ExecutorUtil;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.scanners.ScanConstants;
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
	private Future<Boolean> futureTask = null;

	@DataBoundConstructor
	public ResultsRetriever(Run<?,?> build, IResultsProvider provider, String scanName) {
		super(build.getParent());
		m_build = build;
		m_provider = provider;
		m_name = scanName;
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
	
	public boolean checkResults(Run<?,?> r) {
		boolean results = false;
		if (futureTask != null && futureTask.isDone()) {
			try {
				results = futureTask.get();
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
						rTemp.getActions().remove(ResultsRetriever.this);
						try {
							rTemp.save();
						} catch (IOException e) {
						}
						String message = com.hcl.appscan.sdk.Messages.getMessage(ScanConstants.SCAN_FAILED, " Scan Name: " + m_name);
						if (m_provider.getMessage() != null  && m_provider.getMessage().trim().length() > 0) message += ", " + m_provider.getMessage();
						rTemp.setDescription(message);
						return true;
					} else if (rTemp.getAllActions().contains(ResultsRetriever.this) && m_provider.hasResults()) {
						rTemp.getActions().remove(ResultsRetriever.this); //We need to remove this action from the build, but getAllActions() returns a read-only list.
						rTemp.addAction(createResults());
						try {
							rTemp.save();
						} catch (IOException e) {
						}
						return true;
					}

					if (m_provider.getMessage() != null) {
						rTemp.setDescription(m_provider.getMessage());
					}

					return false;
				}
			};
			futureTask = (Future<Boolean>) ExecutorUtil.submitTask(callableTask);
		}

		return results;
	}

	private ScanResults createResults() {
		return new ScanResults(
				m_build,
				m_provider,
				m_name,
				m_provider.getStatus(),
				m_provider.getFindingsCount(),
				m_provider.getHighCount(),
				m_provider.getMediumCount(),
				m_provider.getLowCount(),
				m_provider.getInfoCount());
	}
}
