/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.presence.CloudPresenceProvider;
import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;

import hudson.Extension;
import hudson.RelativePath;
import hudson.model.ItemGroup;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import hudson.util.VariableResolver;

public class DynamicAnalyzer extends Scanner {

	private static final String DYNAMIC_ANALYZER = "Dynamic Analyzer"; //$NON-NLS-1$
	
	private String m_loginUser;
	private Secret m_loginPassword;
	private String m_presenceId;
	private String m_scanFile;
	private String m_testPolicy;
	private String m_scanType;
	private String m_optimization;
	private String m_extraField;
	
	@Deprecated
	public DynamicAnalyzer(String target) {
		this(target, false, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY); 
	}
	
	@Deprecated
	public DynamicAnalyzer(String target, boolean hasOptions, String loginUser, String loginPassword, String presenceId, String scanFile, 
			String testPolicy, String scanType,String optimization, String extraField) {
		super(target, hasOptions);
		m_loginUser = loginUser;
		m_loginPassword = Secret.fromString(loginPassword);
		m_presenceId = presenceId;
		m_scanFile = scanFile;
		m_testPolicy = EMPTY;
		m_scanType = scanFile != null && !scanFile.equals(EMPTY) ? CUSTOM : scanType;
		m_optimization = optimization;
		m_extraField = extraField;
	}
	
	@DataBoundConstructor
	public DynamicAnalyzer(String target, boolean hasOptions) {
		super(target, hasOptions);
		m_loginUser = EMPTY;
		m_loginPassword = Secret.fromString(EMPTY);
		m_presenceId = EMPTY;
		m_scanFile = EMPTY;
		m_testPolicy = EMPTY;
		m_scanType = EMPTY;
		m_optimization = EMPTY;
		m_extraField = EMPTY;
	}
	
	@DataBoundSetter
	public void setLoginUser(String loginUser) {
		m_loginUser = loginUser;
	}
	
	public String getLoginUser() {
		return m_loginUser;
	}
	
	@DataBoundSetter
	public void setLoginPassword(String loginPassword) {
		m_loginPassword = Secret.fromString(loginPassword);
	}
	
	public String getLoginPassword() {
		return Secret.toString(m_loginPassword);
	}

	@DataBoundSetter
	public void setPresenceId(String presenceId) {
		m_presenceId = presenceId;
	}
	
	public String getPresenceId() {
		return m_presenceId;
	}
	
	@DataBoundSetter
	public void setScanFile(String scanFile) {
		m_scanFile = scanFile;
	}
	
	public String getScanFile() {
		return m_scanFile;
	}
	
	@DataBoundSetter
	public void setTestPolicy(String testPolicy) {
		m_testPolicy = testPolicy;
	}
	
	public String getTestPolicy() {
		return m_testPolicy;
	}
	
	@DataBoundSetter
	public void setScanType(String scanType) {
		m_scanType = m_scanFile != null && !m_scanFile.equals(EMPTY) ? CUSTOM : scanType;
	}
	
	public String getScanType() {
		return m_scanType;
	}

	@DataBoundSetter
	public void setOptimization(String optimization) {
		m_optimization = optimization;
	}
	
	public String getOptimization() {
		return m_optimization;
	}
	
	@DataBoundSetter
	public void setExtraField(String extraField) {
		m_extraField = extraField;
	}
	
	public String getExtraField() {
		return m_extraField;
	}
	
	@Override
	public String getType() {
		return DYNAMIC_ANALYZER;
	}
	
	@Override
	public Map<String, String> getProperties(VariableResolver<String> resolver) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(TARGET, getTarget());
		properties.put(LOGIN_USER, m_loginUser);
		properties.put(LOGIN_PASSWORD, Secret.toString(m_loginPassword));
		properties.put(PRESENCE_ID, m_presenceId);
		properties.put(SCAN_FILE, resolver == null ? m_scanFile : resolvePath(m_scanFile, resolver));
		properties.put(TEST_POLICY, m_testPolicy);
		properties.put(SCAN_TYPE, m_scanType);
		properties.put(OPTIMIZATION, m_optimization.equals("Normal")? "false":"true");
		properties.put(EXTRA_FIELD, m_extraField);
		return properties;
	}
	
	@Symbol("dynamic_analyzer") //$NON-NLS-1$
	@Extension
	public static final class DescriptorImpl extends ScanDescriptor {
		
		@Override
		public String getDisplayName() {
			return DYNAMIC_ANALYZER;
		}
		
		public ListBoxModel doFillScanTypeItems() {
			ListBoxModel model = new ListBoxModel();
			model.add(Messages.option_staging(), STAGING);
			model.add(Messages.option_production(), PRODUCTION);
			return model;
		}
		
		public ListBoxModel doFillOptimizationItems() {
			ListBoxModel model = new ListBoxModel();
			model.add(Messages.option_normal(), NORMAL);
			model.add(Messages.option_optimized(), OPTIMIZED);
			return model;
		}
		
    	public ListBoxModel doFillPresenceIdItems(@RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) { //$NON-NLS-1$
    		IAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials, context);
    		Map<String, String> presences = new CloudPresenceProvider(authProvider).getPresences();
    		ListBoxModel model = new ListBoxModel();
    		model.add(""); //$NON-NLS-1$
    		
    		if(presences != null) {
	    		for(Map.Entry<String, String> entry : presences.entrySet())
	    			model.add(entry.getValue(), entry.getKey());
    		}
    		return model;
    	}
		
    	public FormValidation doCheckScanFile(@QueryParameter String scanFile) {
    		if(!scanFile.trim().equals(EMPTY) && !scanFile.endsWith(TEMPLATE_EXTENSION) && !scanFile.endsWith(TEMPLATE_EXTENSION2))
    			return FormValidation.error(Messages.error_invalid_template_file());
    		return FormValidation.ok();
    	}
    	
    	public FormValidation doCheckTarget(@QueryParameter String target) {
    		return FormValidation.validateRequired(target);
    	}
	}
}

