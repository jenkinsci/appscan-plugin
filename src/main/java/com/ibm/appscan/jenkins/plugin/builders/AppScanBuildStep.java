/**
 * @ Copyright IBM Corporation 2016.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.builders;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.ItemGroup;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.ibm.appscan.jenkins.plugin.Messages;
import com.ibm.appscan.jenkins.plugin.ScanFactory;
import com.ibm.appscan.jenkins.plugin.actions.ResultsRetriever;
import com.ibm.appscan.jenkins.plugin.actions.ScanResultsTrend;
import com.ibm.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.ibm.appscan.jenkins.plugin.auth.ASoCCredentials;
import com.ibm.appscan.jenkins.plugin.results.ResultsInspector;
import com.ibm.appscan.jenkins.plugin.results.FailureCondition;
import com.ibm.appscan.plugin.core.CoreConstants;
import com.ibm.appscan.plugin.core.app.CloudApplicationProvider;
import com.ibm.appscan.plugin.core.auth.IAuthenticationProvider;
import com.ibm.appscan.plugin.core.error.InvalidTargetException;
import com.ibm.appscan.plugin.core.error.ScannerException;
import com.ibm.appscan.plugin.core.logging.DefaultProgress;
import com.ibm.appscan.plugin.core.logging.IProgress;
import com.ibm.appscan.plugin.core.logging.Message;
import com.ibm.appscan.plugin.core.results.IResultsProvider;
import com.ibm.appscan.plugin.core.scan.IScan;
import com.ibm.appscan.plugin.core.utils.SystemUtil;

public class AppScanBuildStep extends Builder {
	
	private String m_name;
	private String m_type;
	private String m_target;
	private String m_application;
	private String m_credentials;
	private List<FailureCondition> m_failureConditions;
	private boolean m_wait;
	private boolean m_failBuild;
	private IAuthenticationProvider m_authProvider;
	
	@DataBoundConstructor
	public AppScanBuildStep(String name, String type, String target, String application, String credentials, List<FailureCondition> failureConditions, boolean failBuild, boolean wait) {
		m_name = (name == null || name.trim().equals("")) ? type.replaceAll(" ", "") + ThreadLocalRandom.current().nextInt(0, 10000) : name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		m_type = type;
		m_target = target;
		m_application = application;
		m_credentials = credentials;
		m_failureConditions = failureConditions;
		m_wait = wait;
		m_failBuild = failBuild;
	}
	
	public String getName() {
		return m_name;
	}
	
	public String getType() {
		return m_type;
	}
	
	public String getTarget() {
		return m_target;
	}
	
	public String getApplication() {
		return m_application;
	}
	
	public String getCredentials() {
		return m_credentials;
	}

	public List<FailureCondition> getFailureConditions() {
		if(m_failureConditions == null)
			return new ArrayList<FailureCondition>();
		return m_failureConditions;
	}
	
	public boolean getFailBuild() {
		return m_failBuild;
	}
	
	public boolean getWait() {
		return m_wait;
	}
	
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }
	
    @Override
    public boolean prebuild(AbstractBuild<?,?> build, BuildListener listener) {
    	m_authProvider = new JenkinsAuthenticationProvider(m_credentials, build.getProject().getParent());
    	Jenkins jenkins = Jenkins.getInstance();
    	String rootDir = jenkins == null ? System.getProperty("java.io.tmpdir") : jenkins.getPluginManager().rootDir.getAbsolutePath(); //$NON-NLS-1$
    	File pluginDir = new File(rootDir, "appscan"); //$NON-NLS-1$
    	System.setProperty(CoreConstants.SACLIENT_INSTALL_DIR, pluginDir.getAbsolutePath());
    	return true;
    }
    
    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    	IProgress progress = new DefaultProgress(listener.getLogger());
    	IScan scan = getScan(progress);
    	
    	try {
    		scan.run();
    	}
    	catch(ScannerException | InvalidTargetException e) {
    		throw new AbortException(Messages.error_running_scan(e.getLocalizedMessage()));
    	}
    	
    	IResultsProvider provider = scan.getResultsProvider();
    	build.addAction(new ResultsRetriever(build, provider, m_name));
    	
		if(m_wait) {
			progress.setStatus(new Message(Message.INFO, Messages.analysis_running()));
			while(!provider.hasResults())
				Thread.sleep(60000);
			if(shouldFailBuild(provider))
				throw new AbortException(Messages.error_threshold_exceeded());
		}
		
		return true;
    }
    
    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?,?> project) {
    	ArrayList<Action> actions = new ArrayList<Action>();
    	actions.add(new ScanResultsTrend(project, m_type, m_name));
    	return actions;
    }
    
    private IScan getScan(IProgress progress) {
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put(CoreConstants.TARGET,  m_target);
		properties.put(CoreConstants.ID,  m_application);
		properties.put(CoreConstants.NAME, m_name + "_" + SystemUtil.getTimeStamp()); //$NON-NLS-1$
		return ScanFactory.getScan(m_type, properties, progress, m_authProvider);
    }
    
    private boolean shouldFailBuild(IResultsProvider provider) {
    	if(!m_failBuild)
    		return false;
    	return new ResultsInspector(m_failureConditions, provider).shouldFail();
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
    	
    	@Override
        public boolean isApplicable(Class<? extends AbstractProject> projectType) {
    		return true;
        }

    	@Override
        public String getDisplayName() {
            return Messages.label_build_step();
        }
    	
    	public ListBoxModel doFillTypeItems() {
    		ListBoxModel model = new ListBoxModel();
    		
    		for(String scanType : ScanFactory.getScanTypes())
    			model.add(scanType);
    		return model;
    	}
    	
    	public ListBoxModel doFillCredentialsItems(@QueryParameter String credentials, @AncestorInPath ItemGroup context) {
    		//We could just use listCredentials() to get the ListBoxModel, but need to work around JENKINS-12802.
    		ListBoxModel model = new ListBoxModel();
    		List<ASoCCredentials> credentialsList = CredentialsProvider.lookupCredentials(ASoCCredentials.class, context,
    				ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
    		boolean hasSelected = false;
    		
    		for(ASoCCredentials creds : credentialsList) {
    			if(creds.getId().equals(credentials))
    				hasSelected = true;
    			String displayName = creds.getDescription();
    			displayName = displayName == null || displayName.equals("") ? creds.getUsername() + "/******" : displayName; //$NON-NLS-1$
    			model.add(new ListBoxModel.Option(displayName, creds.getId(), creds.getId().equals(credentials))); //$NON-NLS-1$
    		}
    		if(!hasSelected)
    			model.add(new ListBoxModel.Option("", "", true)); //$NON-NLS-1$ //$NON-NLS-2$
    		return model;
    	}
    	
    	public ListBoxModel doFillApplicationItems(@QueryParameter String credentials, @AncestorInPath ItemGroup context) {
    		IAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials, context);
    		Map<String, String> applications = new CloudApplicationProvider(authProvider).getApplications();
    		ListBoxModel model = new ListBoxModel();
    		
    		if(applications != null) {
	    		for(Map.Entry<String, String> entry : applications.entrySet())
	    			model.add(entry.getValue(), entry.getKey());
    		}
    		return model;
    	}
    	
    	public FormValidation doCheckCredentials(@QueryParameter String credentials, @AncestorInPath ItemGroup context) {
    		if(credentials.trim().equals("")) //$NON-NLS-1$
    			return FormValidation.errorWithMarkup(Messages.error_no_creds("/credentials")); //$NON-NLS-1$
    		
    		IAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials, context);
    		if(authProvider.isTokenExpired())
    			return FormValidation.errorWithMarkup(Messages.error_token_expired("/credentials")); //$NON-NLS-1$
    			
    		return FormValidation.ok();
    	}
    	
    	public FormValidation doCheckApplication(@QueryParameter String application) {
    		return FormValidation.validateRequired(application);
    	}
    	
    	public FormValidation doCheckTarget(@QueryParameter String target) {
    		return FormValidation.validateRequired(target);
    	}
    }
}
