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
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;
import java.util.Comparator;
import java.util.HashMap;

import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.app.ASEApplicationProvider;
import com.hcl.appscan.sdk.auth.IASEAuthenticationProvider;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.configuration.ConfigurationProviderFactory;
import com.hcl.appscan.sdk.configuration.IComponent;
import com.hcl.appscan.sdk.error.InvalidTargetException;
import com.hcl.appscan.sdk.error.ScannerException;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.logging.Message;
import com.hcl.appscan.sdk.logging.StdOutProgress;
import com.hcl.appscan.sdk.results.ASEResultsProvider;
import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.sdk.scan.IScan;

import com.hcl.appscan.sdk.utils.SystemUtil;
import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.ScanFactory;
import com.hcl.appscan.jenkins.plugin.actions.ResultsRetriever;
import com.hcl.appscan.jenkins.plugin.auth.ASEJenkinsAuthenticationProvider;
import com.hcl.appscan.jenkins.plugin.auth.ASECredentials;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.hcl.appscan.jenkins.plugin.results.FailureCondition;
import com.hcl.appscan.jenkins.plugin.results.ResultsInspector;
import com.hcl.appscan.jenkins.plugin.util.ScanProgress;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.ItemGroup;
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
	private static final String ASE_DYNAMIC_ANALYZER = "AppScan Enterprise Dynamic Analyzer";

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
	private String m_password;
	
	private IAuthenticationProvider m_authProvider;
	private static final File JENKINS_INSTALL_DIR = new File(System.getProperty("user.dir"), ".appscan"); //$NON-NLS-1$ //$NON-NLS-2$

	@Deprecated
	public AppScanEnterpriseBuildStep(String credentials, String application, String target, String folder, String testPolicy, String template, 
		    String exploreData, String agent, String jobName, boolean email, boolean wait, boolean failBuild, 
		    List<FailureCondition> failureConditions) {
		
		m_credentials = credentials;
		m_application = application;
		m_target = target;
		m_folder = folder;
		m_testPolicy = testPolicy;
		m_template = template;
		m_exploreData = exploreData;
		m_agent = agent;
		m_jobName = (String) ((jobName == null || jobName.trim().equals("")) ? String.valueOf(ThreadLocalRandom.current().nextInt(0, 10000)) : jobName); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$		
		m_email = email;
		m_wait = wait;
		m_failBuild = failBuild;
		m_failureConditions = failureConditions;
	}

	@DataBoundConstructor
	public AppScanEnterpriseBuildStep(String credentials, String folder, String testPolicy, String template,  
		 String loginType, String trafficFile, String accessId, String secretKey, String jobName) {
		
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
		
		m_loginType = loginType;
		m_trafficFile = trafficFile;
		m_userName = accessId;
		m_password = secretKey;
		
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
	
	public String getLoginType() {
		return m_loginType;
	}
	
	public String getTrafficFile() {
		if(!m_loginType.equals("Recorded"))
			return "";
		return m_trafficFile;
	}
	
	public String getAccessId() {
		if(!m_loginType.equals("Automatic"))
			return "";
		return m_userName;
	}
	
	public String getSecretKey() {
		if(!m_loginType.equals("Automatic"))
			return "";
		return m_password;
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
        public void setExploreData(String exploreData){
            m_exploreData = exploreData;
        }
        
        public String getExploreData(){
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
		performScan((Run<?, ?>) build, launcher, listener);
		return true;
	}

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		performScan((Run<?, ?>) run, launcher, listener);
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
	
	public String isLoginType(String testTypeName) {
		return m_loginType.equalsIgnoreCase(testTypeName) ? "true" : "";
	}

	private Map<String, String> getScanProperties(Run<?, ?> build, TaskListener listener) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(CoreConstants.SCANNER_TYPE, ASE_DYNAMIC_ANALYZER);
		properties.put("credentials", m_credentials);	
		properties.put("application", m_application);
		properties.put("startingURL", m_target);
		properties.put("folder", m_folder);
		properties.put("testPolicyId", m_testPolicy);
		properties.put("templateId", m_template);
		properties.put("agentServer", m_agent);
		properties.put("exploreData", m_exploreData);
		properties.put("loginType", m_loginType);
		if(m_loginType.equals("Recorded")) {
			properties.put("trafficFile", m_trafficFile);
		} else if (m_loginType.equals("Automatic")) {
			properties.put("userName", m_userName);
			properties.put("password",m_password);			
		}
				
		properties.put(CoreConstants.SCAN_NAME, m_jobName + "_" + SystemUtil.getTimeStamp());
		properties.put(CoreConstants.EMAIL_NOTIFICATION, Boolean.toString(m_email));
		properties.put("APPSCAN_IRGEN_CLIENT", "Jenkins");
		return properties;
	}

	private void shouldFailBuild(IResultsProvider provider, Run<?, ?> build) throws AbortException, IOException {
		if (!m_failBuild)
			return;
		String failureMessage = Messages.error_threshold_exceeded();
		try {
			List<FailureCondition> failureConditions = m_failureConditions;
			if (new ResultsInspector(failureConditions, provider).shouldFail()) {
				build.setDescription(failureMessage);
				throw new AbortException(failureMessage);
			}
		} catch (NullPointerException e) {
			throw new AbortException(Messages.error_checking_results(provider.getStatus()));
		}
	}

	private void performScan(Run<?, ?> build, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		Map<String, String> properties = getScanProperties(build, listener);
		m_authProvider = new ASEJenkinsAuthenticationProvider(properties.get("credentials"),
				build.getParent().getParent());
		final IProgress progress = new ScanProgress(listener);
		final boolean suspend = m_wait;
		final IScan scan = ScanFactory.createScan(properties, progress, m_authProvider); // joy Call ASEScanFactory
																							// directly

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

					IResultsProvider provider = new ASEResultsProvider(scan.getScanId(), scan.getType(),
							scan.getServiceProvider(), progress);
					provider.setReportFormat(scan.getReportFormat());
					if (suspend) {
						progress.setStatus(new Message(Message.INFO, Messages.analysis_running()));
						String status = provider.getStatus();

						while(status != null && (status.equalsIgnoreCase("Waiting to Run") 
								|| status.equalsIgnoreCase("Starting") ||status.equalsIgnoreCase("Running"))) {
							Thread.sleep(60000);
							status = provider.getStatus();
						}
					}

					return provider;
				} catch (ScannerException | InvalidTargetException | InterruptedException e) {
					throw new AbortException(Messages.error_running_scan(e.getLocalizedMessage()));
				}
			}
		});

		provider.setProgress(new StdOutProgress()); // Avoid serialization problem with StreamBuildListener.
		build.addAction(new ResultsRetriever(build, provider, m_jobName));

		if (m_wait)
			shouldFailBuild(provider, build);
	}

	private void setInstallDir() {
		if (SystemUtil.isWindows() && System.getProperty("user.home").toLowerCase().indexOf("system32") >= 0) {
			System.setProperty(CoreConstants.SACLIENT_INSTALL_DIR, JENKINS_INSTALL_DIR.getPath());
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
			return Messages.label_asebuild_step();
		}

		public ListBoxModel doFillCredentialsItems(@QueryParameter String credentials,
				@AncestorInPath ItemGroup<?> context) {

			ListBoxModel model = new ListBoxModel();
			List<ASECredentials> credentialsList = CredentialsProvider.lookupCredentials(ASECredentials.class, context,
					ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
			boolean hasSelected = false;

			for (ASECredentials creds : credentialsList) {
				if (creds.getId().equals(credentials))
					hasSelected = true;
				String displayName = creds.getDescription();
				displayName = displayName == null || displayName.equals("") ? creds.getUsername() + "/******" //$NON-NLS-1$
						: displayName;
				model.add(new ListBoxModel.Option(displayName, creds.getId(), creds.getId().equals(credentials))); // $NON-NLS-1$
			}
			if (!hasSelected)
				model.add(new ListBoxModel.Option("", "", true)); //$NON-NLS-1$ //$NON-NLS-2$
			return model;
		}

		public ListBoxModel doFillApplicationItems(@QueryParameter String credentials,
				@AncestorInPath ItemGroup<?> context) {
			IASEAuthenticationProvider authProvider = new ASEJenkinsAuthenticationProvider(credentials, context);
			Map<String, String> applications = new ASEApplicationProvider(authProvider).getApplications();
			ListBoxModel model = new ListBoxModel();
			model.add("");

			if (applications != null) {
				List<Entry<String, String>> list = sortApplications(applications.entrySet());

				for (Map.Entry<String, String> entry : list)
					model.add(entry.getValue(), entry.getKey());
			}
			return model;
		}

		private List<Entry<String, String>> sortApplications(Set<Entry<String, String>> set) {
			List<Entry<String, String>> list = new ArrayList<>(set);
			if (list.size() > 1) {
				Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
					public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
						return (o1.getValue().toLowerCase()).compareTo(o2.getValue().toLowerCase());
					}
				});
			}
			return list;
		}

		public ListBoxModel doFillFolderItems(@QueryParameter String credentials,
				@AncestorInPath ItemGroup<?> context) { // $NON-NLS-1$
			IASEAuthenticationProvider authProvider = new ASEJenkinsAuthenticationProvider(credentials, context);
			IComponent componentProvider = ConfigurationProviderFactory.getScanner("Folder", authProvider);
			Map<String, String> items = componentProvider.getComponents();
			ListBoxModel model = new ListBoxModel();
			model.add(""); //$NON-NLS-1$

			if (items != null) {
				for (Map.Entry<String, String> entry : items.entrySet())
					model.add(entry.getValue(), entry.getKey());
			}
			return model;
		}

		public ListBoxModel doFillTestPolicyItems(@QueryParameter String credentials,
				@AncestorInPath ItemGroup<?> context) { // $NON-NLS-1$
			IASEAuthenticationProvider authProvider = new ASEJenkinsAuthenticationProvider(credentials, context);
			IComponent componentProvider = ConfigurationProviderFactory.getScanner("TestPolicies", authProvider);
			Map<String, String> items = componentProvider.getComponents();
			ListBoxModel model = new ListBoxModel();
			model.add(""); //$NON-NLS-1$

			if (items != null) {
				for (Map.Entry<String, String> entry : items.entrySet())
					model.add(entry.getValue(), entry.getKey());
			}
			return model;
		}

		public ListBoxModel doFillTemplateItems(@QueryParameter String credentials,
				@AncestorInPath ItemGroup<?> context) { // $NON-NLS-1$
			IASEAuthenticationProvider authProvider = new ASEJenkinsAuthenticationProvider(credentials, context);
			IComponent componentProvider = ConfigurationProviderFactory.getScanner("Template", authProvider);
			Map<String, String> items = componentProvider.getComponents();
			ListBoxModel model = new ListBoxModel();
			model.add(""); //$NON-NLS-1$

			if (items != null) {
				for (Map.Entry<String, String> entry : items.entrySet())
					model.add(entry.getValue(), entry.getKey());
			}
			return model;
		}

		public ListBoxModel doFillAgentItems(@QueryParameter String credentials,
				@AncestorInPath ItemGroup<?> context) { // $NON-NLS-1$
			IASEAuthenticationProvider authProvider = new ASEJenkinsAuthenticationProvider(credentials, context);
			IComponent componentProvider = ConfigurationProviderFactory.getScanner("Agent", authProvider);
			Map<String, String> items = componentProvider.getComponents();
			ListBoxModel model = new ListBoxModel();
			model.add(""); //$NON-NLS-1$

			if (items != null) {
				for (Map.Entry<String, String> entry : items.entrySet())
					model.add(entry.getValue(), entry.getKey());
			}
			return model;
		}

		public FormValidation doCheckCredentials(@QueryParameter String credentials,
				@AncestorInPath ItemGroup<?> context) {
			if (credentials.trim().equals("")) //$NON-NLS-1$
				return FormValidation.errorWithMarkup(Messages.error_no_creds("/credentials")); //$NON-NLS-1$

			IAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials, context);
			if (authProvider.isTokenExpired())
				return FormValidation.errorWithMarkup(Messages.error_token_expired("/credentials")); //$NON-NLS-1$

			return FormValidation.ok();
		}

		public FormValidation doCheckTemplate(@QueryParameter String template) {
			return FormValidation.validateRequired(template);
		}
		
		public FormValidation doCheckFolder(@QueryParameter String folder) {
			return FormValidation.validateRequired(folder);
		}
		
		public FormValidation doCheckTestPolicy(@QueryParameter String testPolicy) {
			return FormValidation.validateRequired(testPolicy);
		}
		
		public FormValidation doCheckJobName(@QueryParameter String jobName) {
			return FormValidation.validateRequired(jobName);
		}
	}
}