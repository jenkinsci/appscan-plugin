/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017,2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.builders;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Comparator;

import javax.annotation.Nonnull;

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
import com.hcl.appscan.sdk.logging.StdOutProgress;
import com.hcl.appscan.sdk.results.ASEResultsProvider;
import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.sdk.results.NonCompliantIssuesResultProvider;
import com.hcl.appscan.sdk.scan.IScan;

import com.hcl.appscan.sdk.utils.SystemUtil;
import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.ScanFactory;
import com.hcl.appscan.jenkins.plugin.actions.ResultsRetriever;
import com.hcl.appscan.jenkins.plugin.auth.ASEJenkinsAuthenticationProvider;
import com.hcl.appscan.jenkins.plugin.auth.ASoCCredentials;
import com.hcl.appscan.jenkins.plugin.auth.ASECredentials;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.hcl.appscan.jenkins.plugin.results.FailureCondition;
import com.hcl.appscan.jenkins.plugin.results.ResultsInspector;
import com.hcl.appscan.jenkins.plugin.scanners.Scanner;
import com.hcl.appscan.jenkins.plugin.scanners.ScannerFactory;
import com.hcl.appscan.jenkins.plugin.util.BuildVariableResolver;
import com.hcl.appscan.jenkins.plugin.util.ScanProgress;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;

public class AppScanEnterpriseBuildStep extends Builder implements SimpleBuildStep, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Scanner m_scanner;
	private String m_name;
	private String m_type;
	private String m_target;
	private String m_credentials;
	private List<FailureCondition> m_failureConditions;
	private boolean m_emailNotification;
	private boolean m_wait;
    private boolean m_failBuildNonCompliance;
	private boolean m_failBuild;
	private IAuthenticationProvider m_authProvider;
	private static final File JENKINS_INSTALL_DIR=new File(System.getProperty("user.dir"),".appscan");//$NON-NLS-1$ //$NON-NLS-2$
	
	@Deprecated
	public AppScanEnterpriseBuildStep(Scanner scanner, String name, String type, String target, String application, String credentials, List<FailureCondition> failureConditions, boolean failBuildNonCompliance, boolean failBuild, boolean wait, boolean email) {
		m_scanner = scanner;
		m_name = (name == null || name.trim().equals("")) ? "" + ThreadLocalRandom.current().nextInt(0, 10000) : name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		m_type = scanner.getType();
		m_target = target;
		
		m_credentials = credentials;
		m_failureConditions = failureConditions;
		m_emailNotification = email;
		m_wait = wait;
        m_failBuildNonCompliance=failBuildNonCompliance;
		m_failBuild = failBuild;
        }
	
	@DataBoundConstructor
	public AppScanEnterpriseBuildStep(Scanner scanner, String name, String type, String application, String credentials) {
		m_scanner = scanner;
		m_name = (name == null || name.trim().equals("")) ? "" + ThreadLocalRandom.current().nextInt(0, 10000) : name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		m_type = scanner.getType();
		m_target = "";
	
		m_credentials = credentials;
		m_emailNotification = false;
		m_wait = false;
        m_failBuildNonCompliance=false;
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
    public void setFailBuildNonCompliance(boolean failBuildNonCompliance){
            m_failBuildNonCompliance=failBuildNonCompliance;
    }
        
    public boolean getFailBuildNonCompliance(){
        return m_failBuildNonCompliance;
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
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        if (m_scanner.getType().equals("AppScan Enterprise Dynamic Analyzer")){
            performASEScan ((Run<?,?>)build, launcher, listener);
            return true;
        }
    	perform((Run<?,?>)build, launcher, listener);
		return true;
    }
    
	@Override
	public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
		perform(run, launcher, listener);
	}
    
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
    	return BuildStepMonitor.NONE;
    }
    
    //To retain backward compatibility
    protected Object readResolve() {
    	if(m_scanner == null && m_type != null)
    		m_scanner = ScannerFactory.getScanner(m_type, m_target);
    	return this;
    }
    
    private Map<String, String> getScanProperties(Run<?,?> build, TaskListener listener) {
    	BuildVariableResolver resolver = build instanceof AbstractBuild ? new BuildVariableResolver((AbstractBuild<?,?>)build, listener) : null;
		Map<String, String> properties = m_scanner.getProperties(resolver);
		properties.put(CoreConstants.SCANNER_TYPE, m_scanner.getType());
		properties.put(CoreConstants.SCAN_NAME, m_name + "_" + SystemUtil.getTimeStamp()); //$NON-NLS-1$
		properties.put(CoreConstants.EMAIL_NOTIFICATION, Boolean.toString(m_emailNotification));
		properties.put("APPSCAN_IRGEN_CLIENT", "Jenkins");
		return properties;
    }
    
    
    private void shouldFailBuild(IResultsProvider provider,Run<?,?> build) throws AbortException, IOException{
    	if(!m_failBuild && !m_failBuildNonCompliance)
    		return ;
        String failureMessage=Messages.error_threshold_exceeded();
		try {
                    List<FailureCondition> failureConditions=m_failureConditions;
                    if (m_failBuildNonCompliance){
                        failureConditions =new ArrayList<>();
                        FailureCondition nonCompliantCondition = new FailureCondition("total", 0);
                        failureConditions.add(nonCompliantCondition);
                        failureMessage=Messages.error_noncompliant_issues();
                    }
	    	if(new ResultsInspector(failureConditions, provider).shouldFail()){
                    build.setDescription(failureMessage);
                    throw new AbortException(failureMessage);
                }
                    
	    } catch(NullPointerException e) {
	    	throw new AbortException(Messages.error_checking_results(provider.getStatus()));
	    }
	}
    
    private void perform(Run<?,?> build, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
    	m_authProvider = new JenkinsAuthenticationProvider(m_credentials, build.getParent().getParent());
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
					setInstallDir();
		    		scan.run();
		    		
                                IResultsProvider provider=new NonCompliantIssuesResultProvider(scan.getScanId(), scan.getType(), scan.getServiceProvider(), progress);
                                provider.setReportFormat(scan.getReportFormat());
		    		if(suspend) {
		    			progress.setStatus(new Message(Message.INFO, Messages.analysis_running()));
		    			String status = provider.getStatus();
		    			
		    			while(status != null && (status.equalsIgnoreCase(CoreConstants.INQUEUE) || status.equalsIgnoreCase(CoreConstants.RUNNING))) {
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

    	provider.setProgress(new StdOutProgress()); //Avoid serialization problem with StreamBuildListener.
    	build.addAction(new ResultsRetriever(build, provider, m_name));
                
        if(m_wait)
            shouldFailBuild(provider,build);	
    }
    
    private void performASEScan(Run<?,?> build, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        Map<String,String> properties=getScanProperties(build, listener);
    	m_authProvider = new ASEJenkinsAuthenticationProvider(properties.get("credentials"), build.getParent().getParent());
    	final IProgress progress = new ScanProgress(listener);
    	final boolean suspend = m_wait;
    	final IScan scan = ScanFactory.createScan(properties, progress, m_authProvider);
        
    	
    	IResultsProvider provider = launcher.getChannel().call(new Callable<IResultsProvider, AbortException>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void checkRoles(RoleChecker arg0) {
			}

			@Override
			public IResultsProvider call() throws AbortException {
				try {
					setInstallDir();
		    		scan.run();
		    		
                                IResultsProvider provider=new ASEResultsProvider(scan.getScanId(), scan.getType(), scan.getServiceProvider(), progress);
                                provider.setReportFormat(scan.getReportFormat());
		    		if(suspend) {
		    			progress.setStatus(new Message(Message.INFO, Messages.analysis_running()));
		    			String status = provider.getStatus();
		    			
		    			while(status != null && (status.equalsIgnoreCase(CoreConstants.INQUEUE) || status.equalsIgnoreCase(CoreConstants.RUNNING))) {
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

    	provider.setProgress(new StdOutProgress()); //Avoid serialization problem with StreamBuildListener.
    	build.addAction(new ResultsRetriever(build, provider, m_name));
                
        if(m_wait)
            shouldFailBuild(provider,build);	
    }
    
    private void setInstallDir() {
    	if (SystemUtil.isWindows() && System.getProperty("user.home").toLowerCase().indexOf("system32")>=0) {
    		System.setProperty(CoreConstants.SACLIENT_INSTALL_DIR, JENKINS_INSTALL_DIR.getPath());
    	}
    }
    
	@Symbol("appscan") //$NON-NLS-1$
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
    	
    	//Retain backward compatibility
    	@Initializer(before = InitMilestone.PLUGINS_STARTED)
    	public static void createAliases() {
    		Items.XSTREAM2.addCompatibilityAlias("com.hcl.appscan.plugin.core.results.CloudResultsProvider", com.hcl.appscan.sdk.results.CloudResultsProvider.class);
            Items.XSTREAM2.addCompatibilityAlias("com.hcl.appscan.plugin.core.scan.CloudScanServiceProvider", com.hcl.appscan.sdk.scan.CloudScanServiceProvider.class);
    	}
    	
    	@Override
        public boolean isApplicable(Class<? extends AbstractProject> projectType) {
    		return true;
        }

    	@Override
        public String getDisplayName() {
            	return "Run AppScan Enterprise Security Test";
        }
    	
    	public ListBoxModel doFillCredentialsItems(@QueryParameter String scanner, @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
    		//We could just use listCredentials() to get the ListBoxModel, but need to work around JENKINS-12802.
		
		if(scanner.equalsIgnoreCase("AppScan Enterprise Dynamic Analyzer")){
		    ListBoxModel model = new ListBoxModel();
		    List<ASECredentials> credentialsList = CredentialsProvider.lookupCredentials(ASECredentials.class, context,
				    ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
		    //boolean hasSelected = false;

		    for(ASECredentials creds : credentialsList) {
			    //if(creds.getId().equals(credentials))
				    //hasSelected = true;
			    String displayName = creds.getDescription();
			    displayName = displayName == null || displayName.equals("") ? creds.getUsername() + "/******" : displayName; //$NON-NLS-1$
			    model.add(new ListBoxModel.Option(displayName, creds.getId(), creds.getId().equals(credentials))); //$NON-NLS-1$
		    }
		    //if(!hasSelected)
		    //	model.add(new ListBoxModel.Option("", "", true)); //$NON-NLS-1$ //$NON-NLS-2$
		    return model;
                }
                else {
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
    	}
    	
    	
    	
    	
    	
    	public FormValidation doCheckCredentials(@QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
    		if(credentials.trim().equals("")) //$NON-NLS-1$
    			return FormValidation.errorWithMarkup(Messages.error_no_creds("/credentials")); //$NON-NLS-1$
    		
    		IAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials, context);
    		if(authProvider.isTokenExpired())
    			return FormValidation.errorWithMarkup(Messages.error_token_expired("/credentials")); //$NON-NLS-1$
    			
    		return FormValidation.ok();
    	}
    	
    	
    }
}

