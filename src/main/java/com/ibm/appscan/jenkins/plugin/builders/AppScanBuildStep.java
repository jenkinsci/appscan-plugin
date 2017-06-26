/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017.
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
import hudson.remoting.Callable;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.app.CloudApplicationProvider;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.error.InvalidTargetException;
import com.hcl.appscan.sdk.error.ScannerException;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.logging.Message;
import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.sdk.scan.IScan;
import com.hcl.appscan.sdk.utils.SystemUtil;
import com.ibm.appscan.jenkins.plugin.Messages;
import com.ibm.appscan.jenkins.plugin.ScanFactory;
import com.ibm.appscan.jenkins.plugin.actions.ResultsRetriever;
import com.ibm.appscan.jenkins.plugin.actions.ScanResultsTrend;
import com.ibm.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.ibm.appscan.jenkins.plugin.auth.ASoCCredentials;
import com.ibm.appscan.jenkins.plugin.results.ResultsInspector;
import com.ibm.appscan.jenkins.plugin.results.FailureCondition;
import com.ibm.appscan.jenkins.plugin.scanners.Scanner;
import com.ibm.appscan.jenkins.plugin.scanners.ScannerFactory;
import com.ibm.appscan.jenkins.plugin.util.BuildVariableResolver;
import com.ibm.appscan.jenkins.plugin.util.ScanProgress;

public class AppScanBuildStep extends Builder implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Scanner m_scanner;
	private String m_name;
	private String m_type;
	private String m_target;
	private String m_application;
	private String m_credentials;
	private List<FailureCondition> m_failureConditions;
	private boolean m_emailNotification;
	private boolean m_wait;
	private boolean m_failBuild;
	private IAuthenticationProvider m_authProvider;
	
	@Deprecated
	public AppScanBuildStep(Scanner scanner, String name, String type, String target, String application, String credentials, List<FailureCondition> failureConditions, boolean failBuild, boolean wait, boolean email) {
		m_scanner = scanner;
		m_name = (name == null || name.trim().equals("")) ? application.replaceAll(" ", "") + ThreadLocalRandom.current().nextInt(0, 10000) : name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		m_type = scanner.getType();
		m_target = target;
		m_application = application;
		m_credentials = credentials;
		m_failureConditions = failureConditions;
		m_emailNotification = email;
		m_wait = wait;
		m_failBuild = failBuild;
	}
	
	@DataBoundConstructor
	public AppScanBuildStep(Scanner scanner, String name, String type, String application, String credentials) {
		m_scanner = scanner;
		m_name = (name == null || name.trim().equals("")) ? application.replaceAll(" ", "") + ThreadLocalRandom.current().nextInt(0, 10000) : name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		m_type = scanner.getType();
		m_target = "";
		m_application = application;
		m_credentials = credentials;
		m_emailNotification = false;
		m_wait = false;
		m_failBuild = false;
	}
	
	public Scanner getScanner() {
		return m_scanner;
	}
	
	public String getName() {
		return m_name;
	}
	
	public String getType() {
		return m_type;
	}
	
	@DataBoundSetter
	public void setTarget(String target) {
		m_target = target;
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

	@DataBoundSetter
	public void setFailureConditions(List<FailureCondition> failureConditions) {
		m_failureConditions = failureConditions;
	}
	
	public List<FailureCondition> getFailureConditions() {
		if(m_failureConditions == null)
			return new ArrayList<FailureCondition>();
		return m_failureConditions;
	}
	
	@DataBoundSetter
	public void setFailBuild(boolean failBuild) {
		m_failBuild = failBuild;
	}
	
	public boolean getFailBuild() {
		return m_failBuild;
	}
	
	@DataBoundSetter
	public void setWait(boolean wait) {
		m_wait = wait;
	}
	
	public boolean getWait() {
		return m_wait;
	}

	@DataBoundSetter
	public void setEmail(boolean emailNotification) {
		m_emailNotification = emailNotification;
	}
	
	public boolean getEmail() {
		return m_emailNotification;
	}
	
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }
	
    @Override
    public boolean prebuild(AbstractBuild<?,?> build, BuildListener listener) {
    	m_authProvider = new JenkinsAuthenticationProvider(m_credentials, build.getProject().getParent());
    	return true;
    }
    
    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    	final IProgress progress = new ScanProgress(listener);
    	final boolean suspend = m_wait;
    	final IScan scan = ScanFactory.createScan(getScanProperties(build, listener), progress, m_authProvider);

    	IResultsProvider provider = launcher.getChannel().call(new Callable<IResultsProvider, AbortException>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void checkRoles(RoleChecker arg0) {
			}

			@Override
			public IResultsProvider call() throws AbortException {
				try {
		    		scan.run();
		    		IResultsProvider provider = scan.getResultsProvider();
		    		
		    		if(suspend) {
		    			progress.setStatus(new Message(Message.INFO, Messages.analysis_running()));
		    			String status = provider.getStatus();
		    			
		    			while(status != null && status.equalsIgnoreCase(CoreConstants.RUNNING)) {
		    				Thread.sleep(60000);
		    				status = provider.getStatus();
		    			}
		    		}
		    		
		    		return provider;
		    	}
		    	catch(ScannerException | InvalidTargetException | InterruptedException e) {
		    		throw new AbortException(Messages.error_running_scan(e.getLocalizedMessage()));
		    	}
			}

		});
    	
    	build.addAction(new ResultsRetriever(build, provider, m_name));
    	
		if(m_wait && shouldFailBuild(provider))
			throw new AbortException(Messages.error_threshold_exceeded());
		
		return true;
    }
    
    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?,?> project) {
    	ArrayList<Action> actions = new ArrayList<Action>();
    	actions.add(new ScanResultsTrend(project, m_type, m_name));
    	return actions;
    }
    
    //To retain backward compatibility
    protected Object readResolve() {
    	if(m_scanner == null && m_type != null)
    		m_scanner = ScannerFactory.getScanner(m_type, m_target);
    	return this;
    }
    
    private Map<String, String> getScanProperties(AbstractBuild<?,?> build, BuildListener listener) {
		Map<String, String> properties = m_scanner.getProperties(new BuildVariableResolver(build, listener));
		properties.put(CoreConstants.SCANNER_TYPE, m_scanner.getType());
		properties.put(CoreConstants.APP_ID,  m_application);
		properties.put(CoreConstants.SCAN_NAME, m_name + "_" + SystemUtil.getTimeStamp()); //$NON-NLS-1$
		properties.put(CoreConstants.EMAIL_NOTIFICATION, Boolean.toString(m_emailNotification));
		return properties;
    }
    
    private boolean shouldFailBuild(IResultsProvider provider) throws AbortException{
    	if(!m_failBuild)
    		return false;
		try {
	    	return new ResultsInspector(m_failureConditions, provider).shouldFail();
	    } catch(NullPointerException e) {
	    	throw new AbortException(Messages.error_checking_results(provider.getStatus()));
	    }
	}

	@Symbol("appscan") //$NON-NLS-1$
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
    	
    	public ListBoxModel doFillCredentialsItems(@QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
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
    	
    	public ListBoxModel doFillApplicationItems(@QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
    		IAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials, context);
    		Map<String, String> applications = new CloudApplicationProvider(authProvider).getApplications();
    		ListBoxModel model = new ListBoxModel();
    		
    		if(applications != null) {
	    		for(Map.Entry<String, String> entry : applications.entrySet())
	    			model.add(entry.getValue(), entry.getKey());
    		}
    		return model;
    	}
    	
    	public FormValidation doCheckCredentials(@QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
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
    }
}

