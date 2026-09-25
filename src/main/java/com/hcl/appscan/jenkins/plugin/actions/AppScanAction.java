/**
 * © Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2019, 2026.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.actions;

import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.RunAction2;

public abstract class AppScanAction implements RunAction2 {

	protected static final String ICON = "/plugin/appscan/images/ASoC.ico"; //$NON-NLS-1$
	protected static final String URL = "https://cloud.appscan.com"; //$NON-NLS-1$
	protected transient Run<?, ?> run;

	//Updated the constructor to accept the Run instead of the Job
	public AppScanAction(Run<?, ?> run) {
		this.run = run;
	}

	//Required by RunAction2: Called dynamically when the action is added to a build
	@Override
	public void onAttached(Run<?, ?> r) {
		this.run = r;
	}

	//Required by RunAction2: Called dynamically by Jenkins upon server restart
	@Override
	public void onLoad(Run<?, ?> r) {
		this.run = r;
	}

	//getter to replace all previous 'm_project' direct accesses
	public Job<?, ?> getProject() {
		return this.run != null ? this.run.getParent() : null;
	}

	//getter for the build itself, which is often needed by subclasses
	public Run<?, ?> getRun() {
		return this.run;
	}

	@Override
	public String getIconFileName() {
		return ICON;
	}

	@Override
	public String getUrlName() {
		return URL;
	}
}
