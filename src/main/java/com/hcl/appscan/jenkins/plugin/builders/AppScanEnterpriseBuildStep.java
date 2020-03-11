/**
 * @ Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.builders;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.results.FailureCondition;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.util.Secret;
import jenkins.tasks.SimpleBuildStep;

public class AppScanEnterpriseBuildStep extends Builder implements SimpleBuildStep, Serializable {

	private static final long serialVersionUID = 1L;

	private String m_credentials;
	private String m_application;
	private String m_target;
	private String m_folder;
	private String m_testPolicy;
	private String m_template;
	private String m_exploreData;
	private String m_agent;
	private String m_jobName;
	private boolean m_email;
	private boolean m_wait;
	private boolean m_failBuild;
	private List<FailureCondition> m_failureConditions;

	//LoginMangement
	private String m_loginType;
	private String m_trafficFile;
	private String m_userName;
	private Secret m_password;
	
	private String m_scanType;

	@DataBoundConstructor
	public AppScanEnterpriseBuildStep(String credentials, String folder, String testPolicy, String template, String jobName) {
		
		m_credentials = credentials;
		m_application = "";
		m_target = "";
		m_folder = folder;
		m_testPolicy = testPolicy;
		m_template = template;
		m_exploreData = "";
		m_agent = "";
		m_jobName = (jobName == null || jobName.trim().equals("")) ? String.valueOf(ThreadLocalRandom.current().nextInt(0, 10000)) : jobName ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		m_email = false;
		m_wait = false;
		m_failBuild = false;
		
		m_loginType = "";
		m_trafficFile = "";
		m_userName = "";
		m_password = Secret.fromString("");
		
		m_scanType = "";
	}
	
	public String getCredentials() {
		return m_credentials;
	}
	
	public String getFolder() {
		return m_folder;
	}
	
	public String getTestPolicy() {
		return m_testPolicy;
	}
	
	public String getTemplate() {
		return m_template;
	}
	
	public String getJobName() {
		return m_jobName;
	}

	@DataBoundSetter
	public void setApplication(String application) {
		m_application = application;
	}

	public String getApplication() {
		return m_application;
	}

	@DataBoundSetter
	public void setTarget(String target) {
		m_target = target;
	}

	public String getTarget() {
		return m_target;
	}
	
	@DataBoundSetter
	public void setLoginType(String loginType) {
		m_loginType = loginType;
	}
	
	public String getLoginType() {
		return m_loginType;
	}
	
	@DataBoundSetter
	public void setTrafficFile(String trafficFile) {
		if("Manual".equals(m_loginType))
			m_trafficFile = trafficFile;
	}
	
	public String getTrafficFile() {
		return m_trafficFile;
	}
	
	@DataBoundSetter
    public void setAccessId(String userName) {	
		m_userName = userName;
    }
 
	public String getAccessId() {
		return m_userName;
	}

	@DataBoundSetter
    public void setSecretKey(String password) {
		m_password = Secret.fromString(password);
    }
	
	public String getSecretKey() {
		return Secret.toString(m_password);
	}
	
	@DataBoundSetter
	public void setScanType(String scanType) {
		 m_scanType = scanType;
	}
	
	public String getScanType() {
		return m_scanType;
	}

	@DataBoundSetter
	public void setExploreData(String exploreData) {
		m_exploreData = exploreData;
	}

	public String getExploreData() {
		return m_exploreData;
	}

	@DataBoundSetter
	public void setAgent(String agent) {
		m_agent = agent;
	}

	public String getAgent() {
		return m_agent;
	}
	
	@DataBoundSetter
	public void setEmail(boolean email) {
		m_email = email;
	}

	public boolean getEmail() {
		return m_email;
	}
	
	@DataBoundSetter
	public void setWait(boolean wait) {
		m_wait = wait;
	}

	public boolean getWait() {
		return m_wait;
	}
	
	@DataBoundSetter
	public void setFailBuild(boolean failBuild) {
		m_failBuild = failBuild;
	}

	public boolean getFailBuild() {
		return m_failBuild;
	}

	@DataBoundSetter
	public void setFailureConditions(List<FailureCondition> failureConditions) {
		m_failureConditions = failureConditions;
	}

	public List<FailureCondition> getFailureConditions() {
		if (m_failureConditions == null)
			return new ArrayList<FailureCondition>();
		return m_failureConditions;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws IOException, InterruptedException {
		return true;
	}

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
	
	public String isLoginType(String loginTypeName) {
		if (m_loginType != null)
			return m_loginType.equalsIgnoreCase(loginTypeName) ? "true" : "";
		else if (loginTypeName.equals("Manual")) { //Default
			return "true";
		}
		return "";
	}
	
	public String isScanType(String scanType) {
		if(m_scanType != null) {
			return m_scanType.equalsIgnoreCase(scanType) ? "true" : "";
		} else if (scanType.equals("1")) { //Default
			return "true";
		}
		return "";
	}

    @Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> projectType) {
			return false;
		}

		@Override
		public String getDisplayName() {
			return "Deprecated: " + Messages.label_asebuild_step();
		}
	}
}