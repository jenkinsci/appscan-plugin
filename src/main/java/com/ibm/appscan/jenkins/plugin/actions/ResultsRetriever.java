/**
 * © Copyright IBM Corporation 2016.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.actions;

import java.io.IOException;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import jenkins.model.RunAction2;

import org.kohsuke.stapler.DataBoundConstructor;

import com.ibm.appscan.jenkins.plugin.Messages;
import com.ibm.appscan.plugin.core.results.IResultsProvider;

public class ResultsRetriever extends AppScanAction implements RunAction2 {

	private final AbstractBuild<?,?> m_build;	
	private IResultsProvider m_provider;
	private String m_name;

	@DataBoundConstructor
	public ResultsRetriever(AbstractBuild<?,?> build, IResultsProvider provider, String scanName) {
		super(build.getProject());
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
	
	public boolean getHasResults() {
		return checkResults(m_build);
	}
	
	public boolean checkResults(Run<?, ?> r) {
		if(r.getAllActions().contains(this) && m_provider.hasResults()) {
			r.getActions().remove(this);  //We need to remove this action from the build, but getAllActions() returns a read-only list.
			r.addAction(createResults());
			try {
				r.save();
			} catch (IOException e) {
			}
			return true;
		}
		return false;
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
