/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2025.
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

import com.hcl.appscan.sdk.results.CloudCombinedResultsProvider;
import com.hcl.appscan.sdk.scanners.ScanConstants;
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
import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.sdk.results.NonCompliantIssuesResultProvider;
import com.hcl.appscan.sdk.scan.IScan;

import com.hcl.appscan.sdk.utils.SystemUtil;
import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.ScanFactory;
import com.hcl.appscan.jenkins.plugin.actions.ResultsRetriever;
import com.hcl.appscan.jenkins.plugin.auth.ASoCCredentials;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.hcl.appscan.jenkins.plugin.results.FailureCondition;
import com.hcl.appscan.jenkins.plugin.results.ResultsInspector;
import com.hcl.appscan.jenkins.plugin.scanners.Scanner;
import com.hcl.appscan.jenkins.plugin.scanners.ScannerFactory;
import com.hcl.appscan.jenkins.plugin.util.BuildVariableResolver;
import com.hcl.appscan.jenkins.plugin.util.ScanProgress;
import com.hcl.appscan.jenkins.plugin.util.JenkinsUtil;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.ItemGroup;
import hudson.model.Result;
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
import hudson.util.VariableResolver;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

public class AppScanBuildStep extends Builder implements SimpleBuildStep, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Scanner m_scanner;
	private String m_name;
	private String m_type;
	private String m_target;
	private String m_application;
	private String m_credentials;
	private List<FailureCondition> m_failureConditions;
	private boolean m_emailNotification;
	private boolean m_personalScan;
        private boolean m_intervention;
	private boolean m_wait;
    	private boolean m_failBuildNonCompliance;
	private boolean m_failBuild;
	private String m_scanStatus;
	private IAuthenticationProvider m_authProvider;
	private static final File JENKINS_INSTALL_DIR=new File(System.getProperty("user.dir"),".appscan");//$NON-NLS-1$ //$NON-NLS-2$
	
	@Deprecated
	public AppScanBuildStep(Scanner scanner, String name, String type, String target, String application, String credentials, List<FailureCondition> failureConditions, boolean failBuildNonCompliance, boolean failBuild, boolean wait, boolean email, boolean personalScan, boolean intervention) {
		m_scanner = scanner;
		m_name = (name == null || name.trim().equals("")) ? application.replaceAll(" ", "") + ThreadLocalRandom.current().nextInt(0, 10000) : name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		m_type = scanner.getType();
		m_target = target;
		m_application = application;
		m_credentials = credentials;
		m_failureConditions = failureConditions;
		m_emailNotification = email;
		m_personalScan = personalScan;
                m_intervention = intervention;
		m_wait = wait;
        	m_failBuildNonCompliance=failBuildNonCompliance;
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
		m_personalScan = false;
                m_intervention = true;
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
	public void setIntervention(boolean intervention) {
		m_intervention = intervention;
	}
	
	public boolean isIntervention() {
		return m_intervention;
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

	@DataBoundSetter
	public void setPersonalScan(boolean personalScan) {
		m_personalScan = personalScan;
	}

	public boolean getPersonalScan() {
		return m_personalScan;
	}
	
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }
    
    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
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
    
  
    private Map<String, String> getScanProperties(Run<?,?> build, TaskListener listener) throws AbortException {

		VariableResolver<String> resolver = build instanceof AbstractBuild ? new BuildVariableResolver((AbstractBuild<?,?>)build, listener) : null;
		if(m_scanner == null){
			throw new AbortException(Messages.error_mobile_analyzer());
		}else{
			Map<String, String> properties = m_scanner.getProperties(resolver);
			properties.put(CoreConstants.SCANNER_TYPE, m_scanner.getType());
			properties.put(CoreConstants.APP_ID, m_application);
			properties.put(CoreConstants.SCAN_NAME, resolver == null ? m_name : Util.replaceMacro(m_name, resolver) + "_" + SystemUtil.getTimeStamp()); //$NON-NLS-1$
			properties.put(CoreConstants.EMAIL_NOTIFICATION, Boolean.toString(m_emailNotification));
			properties.put(CoreConstants.PERSONAL_SCAN, Boolean.toString(m_personalScan));
			properties.put("FullyAutomatic", Boolean.toString(!m_intervention));
			properties.put("APPSCAN_IRGEN_CLIENT", "Jenkins");
			properties.put("APPSCAN_CLIENT_VERSION", Jenkins.VERSION);
			properties.put("IRGEN_CLIENT_PLUGIN_VERSION", JenkinsUtil.getPluginVersion());
			properties.put("ClientType", JenkinsUtil.getClientType());
            		properties.put(CoreConstants.SERVER_URL,m_authProvider.getServer());
            		properties.put(CoreConstants.ACCEPT_INVALID_CERTS,Boolean.toString(m_authProvider.getacceptInvalidCerts()));
			return properties;
		}
	}

    private void shouldFailBuild(IResultsProvider provider,Run<?,?> build, IProgress progress) throws AbortException, IOException{
    	if(!m_failBuild && !m_failBuildNonCompliance)
    		return ;
        String failureMessage=Messages.error_threshold_exceeded();
		try {
                    List<FailureCondition> failureConditions=m_failureConditions;
                    progress.setStatus(new Message(Message.INFO, Messages.fail_build_check()));
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
        Map<String, String> properties = getScanProperties(build,listener);
        boolean isAppScan360 = ((JenkinsAuthenticationProvider) m_authProvider).isAppScan360();

        m_scanner.validateSettings((JenkinsAuthenticationProvider) m_authProvider,properties, progress, isAppScan360);

        if (properties.containsKey(CoreConstants.OPEN_SOURCE_ONLY)) {
            progress.setStatus(new Message(Message.WARNING, Messages.warning_sca()));
            m_scanner = ScannerFactory.getScanner(Scanner.SOFTWARE_COMPOSITION_ANALYZER, properties.get(CoreConstants.TARGET));
            properties.put(CoreConstants.SCANNER_TYPE, CoreConstants.SOFTWARE_COMPOSITION_ANALYZER);
        }


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
		    		
                                IResultsProvider provider=scan.getResultsProvider(true);
                                provider.setReportFormat(scan.getReportFormat());
		    		if(suspend) {
		    			progress.setStatus(new Message(Message.INFO, Messages.analysis_running()));
		    			m_scanStatus = provider.getStatus();
		    			
                                        int requestCounter=0;
		    			while(m_scanStatus != null && (m_scanStatus.equalsIgnoreCase(CoreConstants.INQUEUE) || m_scanStatus.equalsIgnoreCase(CoreConstants.RUNNING) || m_scanStatus.equalsIgnoreCase(CoreConstants.UNKNOWN)) && requestCounter<10) {
                                            Thread.sleep(60000);
                                            if(m_scanStatus.equalsIgnoreCase(CoreConstants.UNKNOWN))
                                                requestCounter++;   // In case of internet disconnect, polling the server 10 times to check the connection has established 
                                            else
                                                requestCounter=0;
                                            m_scanStatus = provider.getStatus();
		    			}
		    		}
		    		
		    		return provider;
		    	}
		    	catch(ScannerException | InvalidTargetException | InterruptedException e) {
		    		throw new AbortException(Messages.error_running_scan(e.getLocalizedMessage()));
		    	}
			}
		});
        
        if(suspend && m_scanStatus == null) // to address the status in association with Master and Slave congifuration
            m_scanStatus = provider.getStatus();

    	if (CoreConstants.FAILED.equalsIgnoreCase(m_scanStatus)) {
            String message = com.hcl.appscan.sdk.Messages.getMessage(ScanConstants.SCAN_FAILED, " Scan Name: " + m_name);
            if (provider.getMessage() != null && provider.getMessage().trim().length() > 0) {
                message += ", " + provider.getMessage();
            }
            build.setDescription(message);
            throw new AbortException(com.hcl.appscan.sdk.Messages.getMessage(ScanConstants.SCAN_FAILED, ("Scan Name: " + m_name)));
    	} else if (CoreConstants.UNKNOWN.equalsIgnoreCase(m_scanStatus)) { // In case of internet disconnect Status is set to unstable.
            progress.setStatus(new Message(Message.ERROR, Messages.error_server_unavailable() + " "+ Messages.check_server(m_authProvider.getServer())));
            build.setDescription(Messages.error_server_unavailable());
            build.setResult(Result.UNSTABLE);
        } else {
            provider.setProgress(new StdOutProgress()); //Avoid serialization problem with StreamBuildListener.
            VariableResolver<String> resolver = build instanceof AbstractBuild ? new BuildVariableResolver((AbstractBuild<?,?>)build, listener) : null;
            String asocAppUrl = m_authProvider.getServer() + "/main/myapps/" + m_application + "/scans/";
            String label;
            if(isAppScan360) {
                label = Messages.label_appscan360_homepage();
            } else {
                label = Messages.label_asoc_homepage();
            }

            build.addAction(new ResultsRetriever(build, provider, resolver == null ? m_name : Util.replaceMacro(m_name, resolver), asocAppUrl, label));

            if(m_wait)
                shouldFailBuild(provider,build, progress);

            if(m_scanStatus != null && !m_scanStatus.isEmpty() && m_scanStatus.equalsIgnoreCase(CoreConstants.PARTIAL_SUCCESS)) {
                throw new AbortException(Messages.error_scan_status_unstable());
            }

            //Scan logs are available only for DAST and SAST scans
            if (!m_type.equals(CoreConstants.SOFTWARE_COMPOSITION_ANALYZER) && m_scanStatus != null && !m_scanStatus.isEmpty() && (m_scanStatus.equalsIgnoreCase(CoreConstants.READY) || m_scanStatus.equalsIgnoreCase(CoreConstants.PARTIAL_SUCCESS))) {
                progress.setStatus(new Message(Message.INFO, Messages.scan_log_location(build.getRootDir().getAbsolutePath())));
                File file = new File(build.getRootDir(), m_name + "_ScanLogs" +SystemUtil.getTimeStamp() + ".zip");
                if (provider instanceof NonCompliantIssuesResultProvider) {
                	((NonCompliantIssuesResultProvider) provider).getScanLogs(file);
                } else if (provider instanceof CloudCombinedResultsProvider) {
                	provider.getScanLogFile(file);
                }
            }
        }
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
		Items.XSTREAM2.addCompatibilityAlias("com.ibm.appscan.jenkins.plugin.builders.AppScanBuildStep", com.hcl.appscan.jenkins.plugin.builders.AppScanBuildStep.class);
    		Items.XSTREAM2.addCompatibilityAlias("com.ibm.appscan.jenkins.plugin.scanners.StaticAnalyzer", com.hcl.appscan.jenkins.plugin.scanners.StaticAnalyzer.class);
    		Items.XSTREAM2.addCompatibilityAlias("com.ibm.appscan.jenkins.plugin.scanners.DynamicAnalyzer", com.hcl.appscan.jenkins.plugin.scanners.DynamicAnalyzer.class);
            Items.XSTREAM2.addCompatibilityAlias("com.hcl.appscan.jenkins.plugin.scanners.SoftwareCompositionAnalyzer", com.hcl.appscan.jenkins.plugin.scanners.SoftwareCompositionAnalyzer.class);
    		Items.XSTREAM2.addCompatibilityAlias("com.hcl.appscan.plugin.core.results.CloudResultsProvider", com.hcl.appscan.sdk.results.CloudResultsProvider.class);
		Items.XSTREAM2.addCompatibilityAlias("com.hcl.appscan.plugin.core.scan.CloudScanServiceProvider", com.hcl.appscan.sdk.scan.CloudScanServiceProvider.class);
    	}
    	
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
        		List<Entry<String , String>> list=sortApplications(applications.entrySet());
    			
	    		for(Map.Entry<String, String> entry : list)
	    			model.add(entry.getValue(), entry.getKey());
    		}
    		return model;
    	}
    	
    	private List<Entry<String , String>> sortApplications(Set<Entry<String , String>> set) {
    		List<Entry<String , String>> list= new ArrayList<>(set);
    		if (list.size()>1) {
    			Collections.sort( list, new Comparator<Map.Entry<String, String>>()
                {
                    public int compare( Map.Entry<String, String> o1, Map.Entry<String, String> o2 )
                    {
                        return (o1.getValue().toLowerCase()).compareTo( o2.getValue().toLowerCase() );
                    }
                } );
    		}
		return list;
    	}
    	
    	public FormValidation doCheckCredentials(@QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
    		if(credentials.trim().equals("")) //$NON-NLS-1$
    			return FormValidation.errorWithMarkup(Messages.error_no_creds("/credentials")); //$NON-NLS-1$
    		
    		IAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials, context);
    		if(authProvider.isTokenExpired())
    			return FormValidation.errorWithMarkup(Messages.error_token_expired("/credentials")); //$NON-NLS-1$
    			
    		return FormValidation.ok();
    	}
    	
    	public FormValidation doCheckApplication(@QueryParameter String application, @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            IAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials, context);
            Map<String, String> applications = new CloudApplicationProvider(authProvider).getApplications();
            if((applications==null || applications.isEmpty()) && !credentials.equals("")){
                return FormValidation.error(Messages.error_application_empty_ui());
            } else {
                return FormValidation.validateRequired(application);
            }
    	}

	public FormValidation doCheckIntervention(@QueryParameter Boolean intervention,@QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
		JenkinsAuthenticationProvider checkAppScan360Connection = new JenkinsAuthenticationProvider(credentials,context);
		if((intervention && checkAppScan360Connection.isAppScan360())){
			return FormValidation.error(Messages.error_allow_intervention_ui());
		}
		return FormValidation.ok();
	}
    }
}

