/**
 * @ Copyright HCL Technologies Ltd. 2019, 2020, 2023.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.builders;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;
import java.util.Comparator;
import java.util.HashMap;

import com.hcl.appscan.sdk.scanners.ScanConstants;
import org.apache.commons.lang.StringEscapeUtils;
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
import com.hcl.appscan.sdk.configuration.ase.ConfigurationProviderFactory;
import com.hcl.appscan.sdk.configuration.ase.IComponent;
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
import com.hcl.appscan.jenkins.plugin.util.BuildVariableResolver;
import com.hcl.appscan.jenkins.plugin.util.ScanProgress;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.ItemGroup;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import hudson.model.AutoCompletionCandidates;
import hudson.remoting.Callable;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import hudson.util.VariableResolver;
import jenkins.tasks.SimpleBuildStep;

public class AppScanEnterpriseBuildStep extends Builder implements SimpleBuildStep, Serializable {

	private static final long serialVersionUID = 1L;
	private static final String ASE_DYNAMIC_ANALYZER = "AppScan Enterprise Dynamic Analyzer";
	private static final String SHOW_ALL = "Show All";

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

	private String m_testOptimization;
	private String m_scanStatus;
    	private String m_description;
    	private String m_contact;
	
	private IAuthenticationProvider m_authProvider;
	private static final File JENKINS_INSTALL_DIR = new File(System.getProperty("user.dir"), ".appscan"); //$NON-NLS-1$ //$NON-NLS-2$

	@DataBoundConstructor
	public AppScanEnterpriseBuildStep(String credentials, String folder, String testPolicy, String template, String jobName) {
		
		m_credentials = credentials;
		m_application = "";
		m_target = "";
		// Post autocomplete feature, we need to explicitly map 
		// folder name to folder id before saving it in
		// job configuration file.
		m_folder = getDescriptor().getFolderId(folder);
		m_testPolicy = testPolicy;
		// Post autocomplete feature, we need to explicitly map 
		// template name to template id before saving it in
		// job configuration file.
		m_template = getDescriptor().getTemplateId(template);
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
		m_testOptimization = "";
        	m_description = "";
        	m_contact = "";
	}
	
	public String getCredentials() {
		// Post autocomplete feature, to handle backward compatibiliy 
		// we have to initialize autocomplete lists explicitly 
		// for already existing jobs.
		if (m_credentials != null && !m_credentials.isEmpty()
				&& getDescriptor().folderMap == null
				&& getDescriptor().applicationMap == null
				&& getDescriptor().templateMap == null) {
			Jenkins jenkins = Jenkins.getInstanceOrNull();
			if(jenkins != null) {
				getDescriptor().setAutoCompleteList(m_credentials, jenkins.getItemGroup());
			}
		}
		return m_credentials;
	}
	
	public String getFolder() {
		// Post autocomplete feature, we need to explicitly map the folder id
		// to folder name before displaying the value in jenkins's job
		// configuration. 
		if (getDescriptor().folderMap != null &&
				getDescriptor().folderMap.get(m_folder) != null) {
			String folder = StringEscapeUtils.unescapeHtml(
					getDescriptor().folderMap.get(m_folder));
			return folder;
		}
		return m_folder;
	}
	
	public String getTestPolicy() {
		return m_testPolicy;
	}
	
	public String getTemplate() {
		// Post autocomplete feature, we need to explicitly map the template id
		// to template name before displaying the value in jenkins's job
		// configuration. 
		if (getDescriptor().templateMap != null &&
				getDescriptor().templateMap.get(m_template) != null) {
        	String template = StringEscapeUtils.unescapeHtml(
        			getDescriptor().templateMap.get(m_template));
        	return template;
		}
		return m_template;
	}
	
	public String getJobName() {
		return m_jobName;
	}

	@DataBoundSetter
	public void setApplication(String application) {
		// Post autocomplete feature, we need to explicitly map 
		// application name to application id before saving it in
		// job configuration file.
		m_application = getDescriptor().getApplicationId(application);
	}

	public String getApplication() {
		// Post autocomplete feature, we need to explicitly map the application
		// id to application name before displaying the value in jenkins's job
		// configuration. 
		if (getDescriptor().applicationMap != null &&
				getDescriptor().applicationMap.get(m_application) != null) {
			String appName = StringEscapeUtils.unescapeHtml(
					getDescriptor().applicationMap.get(m_application));
			return appName;
		}
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
	public void setTestOptimization(String testOptimization) {
		m_testOptimization = testOptimization;
	}

	public String getTestOptimization() {
		return m_testOptimization;
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
    	public void setDescription(String description) {
        	m_description = description;
    	}

    	public String getDescription() {
        	return m_description;
    	}

    	@DataBoundSetter
    	public void setContact(String contact) {
        	m_contact = contact;
    	}

    	public String getContact() {
        	return m_contact;
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

	public String isTestOptimization(String testOptimizationLevel) {
		if (m_testOptimization != null) {
			return  m_testOptimization.equalsIgnoreCase(testOptimizationLevel) ? "true" : "";
		} else if (testOptimizationLevel.equals("1")) { //Default
			return "true";
		}
		return "";
	}

	private Map<String, String> getScanProperties(Run<?, ?> build, TaskListener listener) {
            VariableResolver<String> resolver = build instanceof AbstractBuild ? new BuildVariableResolver((AbstractBuild<?,?>)build, listener) : null;
            
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(CoreConstants.SCANNER_TYPE, ASE_DYNAMIC_ANALYZER);
                properties.put("credentials", m_credentials);
                properties.put("testPolicyId", m_testPolicy);
                properties.put("agentServer", m_agent);
                properties.put("loginType", m_loginType);
                properties.put("scanType", m_scanType);
                properties.put("testOptimization", m_testOptimization);
                properties.put(CoreConstants.EMAIL_NOTIFICATION, Boolean.toString(m_email));
                
                if(resolver == null) {
                    properties.put("application", m_application);
                    properties.put("startingURL", m_target);
                    properties.put("folder", m_folder);
                    properties.put("templateId", m_template);
                    properties.put("exploreData", m_exploreData);
                    properties.put(CoreConstants.SCAN_NAME, m_jobName + "_" + SystemUtil.getTimeStamp());
                    properties.put("description", m_description);
                    properties.put("contact", m_contact);
                }
                else {
                    properties.put("application", Util.replaceMacro(m_application, resolver));
                    properties.put("startingURL", Util.replaceMacro(m_target, resolver));
                    properties.put("folder", Util.replaceMacro(m_folder, resolver));
                    properties.put("templateId", Util.replaceMacro(m_template, resolver));
                    properties.put("exploreData", m_exploreData.equals("") ? m_exploreData : resolvePath(m_exploreData, resolver));
                    properties.put(CoreConstants.SCAN_NAME, Util.replaceMacro(m_jobName, resolver) + "_" + SystemUtil.getTimeStamp()); //$NON-NLS-1$
                    properties.put("description", Util.replaceMacro(m_description, resolver));
                    properties.put("contact", Util.replaceMacro(m_contact, resolver));
                }

		if (m_loginType != null) {
                    if (m_loginType.equals("Manual")) {
                        properties.put("trafficFile", (resolver == null || m_trafficFile.equals(""))? m_trafficFile : resolvePath(m_trafficFile, resolver));
                    } else if (m_loginType.equals("Automatic")) {
                        properties.put("userName", resolver == null ? m_userName : Util.replaceMacro(m_userName, resolver));
                        properties.put("password", resolver == null ? Secret.toString(m_password) : Util.replaceMacro(Secret.toString(m_password), resolver));
                    }
                }
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

    private boolean checkURLAccessibility(String URL) throws IOException {
        try {
            URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception e){
            throw new AbortException(Messages.error_url_validation());
        }
    }

    	private String getUpdatedApplicationId(Map<String, String> application){
        	if(application != null) {
            	for(Map.Entry<String, String> entry : application.entrySet()){
                	String appName = StringEscapeUtils.unescapeHtml(entry.getValue());
                	if(appName != null && appName.equals(m_application)) {
                    	return entry.getKey();
                		}
            		}
        	}
        	return null;
    	}

	private void performScan(Run<?, ?> build, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		Map<String, String> properties = getScanProperties(build, listener);

        if (!checkURLAccessibility(properties.get("startingURL"))) {
            throw new AbortException(Messages.error_url_validation());
        }

        m_authProvider = new ASEJenkinsAuthenticationProvider(properties.get("credentials"),
				build.getParent().getParent());
		final IProgress progress = new ScanProgress(listener);
		final boolean suspend = m_wait;
        	if(m_application.equals(getApplication())){
		// indicating that we have an application name, not an id, so we need to fetch the id
            		IASEAuthenticationProvider authProvider = (IASEAuthenticationProvider) m_authProvider;
            		Map<String, String> appList = new ASEApplicationProvider(authProvider).getApplications();
            		m_application = getUpdatedApplicationId(appList);
            		properties.put("application", m_application);
            		getDescriptor().applicationMap = appList;
        	}
		final IScan scan = ScanFactory.createScan(properties, progress, m_authProvider); // Call ASEScanFactory directly

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
							scan.getServiceProvider(), progress, scan.getName());
					provider.setReportFormat(scan.getReportFormat());
					try {
						String ASE_SCAN_STATS = "/Jobs/QuickScanStats.aspx?fiid=%s";
						URL url = new URL(m_authProvider.getServer() + String.format(ASE_SCAN_STATS, scan.getScanId()));
						progress.setStatus(new Message(Message.INFO, Messages.logs_link(scan.getScanId(), new URL(url.getProtocol(), url.getHost(), url.getFile()).toString())));
					} catch (MalformedURLException e) {
						progress.setStatus(new Message(Message.ERROR, Messages.error_malformed_url(m_authProvider.getServer())));
					}
					if (suspend) {
						progress.setStatus(new Message(Message.INFO, Messages.analysis_running()));
						m_scanStatus = provider.getStatus();

						while(m_scanStatus != null && (m_scanStatus.equalsIgnoreCase("Waiting to Auto Run") || m_scanStatus.equalsIgnoreCase("Waiting to Run")
								|| m_scanStatus.equalsIgnoreCase("Starting") || m_scanStatus.equalsIgnoreCase("Running")
								|| m_scanStatus.equals("Post Processing") || m_scanStatus.equals("Waiting to Generate Results") || m_scanStatus.equals("Generating Results"))) {
							Thread.sleep(60000);
							m_scanStatus = provider.getStatus();
						}
					}
					return provider;
				} catch (ScannerException | InvalidTargetException | InterruptedException e) {
					progress.setStatus(new Message(Message.INFO, Messages.label_ase_homepage() + ": " + m_authProvider.getServer()));
					throw new AbortException(Messages.error_running_scan(e.getLocalizedMessage()));
				}
			}
		});

		if (CoreConstants.FAILED.equalsIgnoreCase(m_scanStatus)) {
			String message = com.hcl.appscan.sdk.Messages.getMessage(ScanConstants.SCAN_FAILED, " Scan Name: " + scan.getName());
			if (provider.getMessage() != null && provider.getMessage().trim().length() > 0) {
				message += ", " + provider.getMessage();
			}
			build.setDescription(message);
			throw new AbortException(com.hcl.appscan.sdk.Messages.getMessage(ScanConstants.SCAN_FAILED, (" Scan Id: " + scan.getScanId() +
					", Scan Name: " + scan.getName())));
		}

		provider.setProgress(new StdOutProgress()); // Avoid serialization problem with StreamBuildListener.
		String aseScanUrl = m_authProvider.getServer();
		String label = Messages.label_ase_homepage();
               
                VariableResolver<String> resolver = build instanceof AbstractBuild ? new BuildVariableResolver((AbstractBuild<?,?>)build, listener) : null;
                String application = resolver == null ? m_application : Util.replaceMacro(m_application, resolver);
		if (application != null && application.trim().length() > 0) {
			String applicationUrl = "/api/pages/applications.html#appProfile/%s/issues";
			aseScanUrl += String.format(applicationUrl, application);
			label = Messages.label_ase_application();
		}
		build.addAction(new ResultsRetriever(build, provider, resolver == null ? m_jobName : Util.replaceMacro(m_jobName, resolver), aseScanUrl, label));

		if (m_wait)
			shouldFailBuild(provider, build);
	}

	private void setInstallDir() {
		if (SystemUtil.isWindows() && System.getProperty("user.home").toLowerCase().indexOf("system32") >= 0) {
			System.setProperty(CoreConstants.SACLIENT_INSTALL_DIR, JENKINS_INSTALL_DIR.getPath());
		}
	}
        
        private String resolvePath(String path, VariableResolver<String> resolver) {
		//First replace any variables in the path
		path = Util.replaceMacro(path, resolver);
		
		//If the path is not absolute, make it relative to the workspace
		File file = new File(path);
		if(!file.isAbsolute()) {
			String targetPath = "${WORKSPACE}" + File.separator + file.getPath();
			targetPath = Util.replaceMacro(targetPath, resolver);
			file = new File(targetPath);
		}

		return file.getAbsolutePath();
	}

	@Symbol("appscanenterprise") //$NON-NLS-1$
    @Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

		private List<Entry<String, String>> sortedTemplateList = null;
		private List<Entry<String, String>> sortedFolderList = null;
		private List<Entry<String, String>> sortedApplicationList = null;
		private Map<String, String> templateMap = null;
		private Map<String, String> folderMap = null;
		private Map<String, String> applicationMap = null;

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
			{
				model.add(new ListBoxModel.Option("", "", true)); //$NON-NLS-1$ //$NON-NLS-2$
				//Resetting autocomplete lists
				sortedTemplateList = null;
				sortedFolderList = null;
				sortedApplicationList = null;
				templateMap = null;
				folderMap = null;
				applicationMap = null;
			}
			else {
				setAutoCompleteList(credentials, context); //set AutoComplete lists if credential is selected
			}
			return model;
		}

		public AutoCompletionCandidates doAutoCompleteApplication(@QueryParameter String value) {
			AutoCompletionCandidates model = new AutoCompletionCandidates();
			if (sortedApplicationList != null) {
				for(Map.Entry<String, String> entry : sortedApplicationList) {
					String appName = StringEscapeUtils.unescapeHtml(entry.getValue());
					if (value.equals(SHOW_ALL)) {
						model.add(appName);
					}
					else {
						if (appName != null && appName.toLowerCase().contains(value.toLowerCase())) {
							model.add(appName);
						}
					}

				}
			}
			return model;
		}

		public AutoCompletionCandidates doAutoCompleteFolder(@QueryParameter String value) {
			AutoCompletionCandidates model = new AutoCompletionCandidates();
			if (sortedFolderList != null) {
				for(Map.Entry<String, String> entry : sortedFolderList) {
					String folderName = StringEscapeUtils.unescapeHtml(entry.getValue());
					if (value.equals(SHOW_ALL)) {
						model.add(folderName);
					}
					else {
						if (folderName != null && folderName.toLowerCase().contains(value.toLowerCase())) {
							model.add(folderName);
						}
					}

				}
			}
			return model;
		}

		public ListBoxModel doFillTestPolicyItems(@QueryParameter String credentials,
				@AncestorInPath ItemGroup<?> context) { // $NON-NLS-1$
			IASEAuthenticationProvider authProvider = new ASEJenkinsAuthenticationProvider(credentials, context);
			IComponent componentProvider = ConfigurationProviderFactory.getScanner("TestPolicies", authProvider);
			Map<String, String> items = componentProvider.getComponents();
			ListBoxModel model = new ListBoxModel();

			if (items != null) {
				List<Entry<String, String>> list = sortComponents(items.entrySet());
				for (Map.Entry<String, String> entry : list)
					model.add(entry.getValue(), entry.getKey());
			}
			return model;
		}

		public AutoCompletionCandidates doAutoCompleteTemplate(@QueryParameter String value) {
			AutoCompletionCandidates model = new AutoCompletionCandidates();
			if (sortedTemplateList != null) {
				for(Map.Entry<String, String> entry : sortedTemplateList) {
					String templateName = StringEscapeUtils.unescapeHtml(entry.getValue());
					if (value.equals(SHOW_ALL)) {
						model.add(templateName);
					}
					else {
						if (templateName != null && templateName.toLowerCase().contains(value.toLowerCase())) {
							model.add(templateName);
						}
					}

				}
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
				List<Entry<String, String>> list = sortComponents(items.entrySet());
				for (Map.Entry<String, String> entry : list)
					model.add(entry.getValue(), entry.getKey());
			}
			return model;
		}

		private List<Entry<String, String>> sortComponents(Set<Entry<String, String>> set) {
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

		public FormValidation doCheckCredentials(@QueryParameter String credentials,
				@AncestorInPath ItemGroup<?> context) {
			if (credentials.trim().equals("")) //$NON-NLS-1$
				return FormValidation.errorWithMarkup(Messages.error_no_creds_ase("/credentials")); //$NON-NLS-1$

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

		//This method will initialize Template, folder and application list.
		private void setAutoCompleteList(String credentials, ItemGroup<?> context) {
			IASEAuthenticationProvider authProvider = new ASEJenkinsAuthenticationProvider(credentials, context);
			IComponent folderComponentProvider = ConfigurationProviderFactory.getScanner("Folder", authProvider);
			folderMap = folderComponentProvider.getComponents();
			sortedFolderList = folderMap != null ? sortComponents(folderMap.entrySet()) : null;
			IComponent templateComponentProvider = ConfigurationProviderFactory.getScanner("Template", authProvider);
			templateMap = templateComponentProvider.getComponents();
			sortedTemplateList = templateMap != null ? sortComponents(templateMap.entrySet()) : null;
			applicationMap = new ASEApplicationProvider(authProvider).getApplications();
			sortedApplicationList = applicationMap != null ? sortComponents(applicationMap.entrySet()) : null;
		}
		
		/**
		 * Gets application Id
		 * @param application This is valid application name 
		 * @return application Id for valid application name otherwise 
		 * application name itself.
		 */
		private String getApplicationId(String application) {
			if (sortedApplicationList != null) {
				for(Map.Entry<String, String> entry : sortedApplicationList) {
					String appName = StringEscapeUtils.unescapeHtml(entry.getValue());
					if (appName != null && appName.equals(application)) {
						return entry.getKey();
					}
				}
			}
			return application;
		}

		/**
		 * Gets folder Id
		 * @param folder This is valid folder name 
		 * @return folder Id for valid folder name otherwise 
		 * folder name itself.
		 */
		private String getFolderId(String folder) {
			if (sortedFolderList != null) {
				for(Map.Entry<String, String> entry : sortedFolderList) {
					String folderName = StringEscapeUtils.unescapeHtml(entry.getValue());
					if(folderName != null && folderName.equals(folder)) {
						return entry.getKey();
					}
				}
			}
			return folder;
		}

		/**
		 * Gets template Id
		 * @param template This is valid template name 
		 * @return template Id for valid template name otherwise 
		 * template name itself.
		 */
		private String getTemplateId(String template) {
			if (sortedTemplateList != null) {
				for(Map.Entry<String, String> entry : sortedTemplateList) {
					String templateName = StringEscapeUtils.unescapeHtml(entry.getValue());
					if(templateName != null && templateName.equals(template)) {
						return entry.getKey();
					}
				}
			}
			return template;
		}
	}
}
