/**
 * © Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.actions;

import hudson.model.Action;
import hudson.model.Job;

public abstract class AppScanAction implements Action {

	protected static final String ICON = "/plugin/appscan/images/ASoC.ico"; //$NON-NLS-1$
    protected static final String SAST = "/plugin/appscan/images/SAST.svg"; //$NON-NLS-1$
    protected static final String SCA = "/plugin/appscan/images/SCA.svg"; //$NON-NLS-1$
    protected static final String DAST = "/plugin/appscan/images/DAST.svg"; //$NON-NLS-1$
	protected static final String URL = "https://cloud.appscan.com"; //$NON-NLS-1$
	protected final Job<?,?> m_project;
	
	public AppScanAction (Job<?,?> project) {
		m_project = project;
	}
	
	@Override
	public String getIconFileName() {
		return ICON;
	}

	@Override
	public String getUrlName() {
		return URL;
	}
	
	public Job<?,?> getProject() {
		return m_project;
	}
}
