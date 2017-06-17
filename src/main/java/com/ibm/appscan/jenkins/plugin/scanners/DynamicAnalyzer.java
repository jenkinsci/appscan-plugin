/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.scanners;

import hudson.Extension;
import hudson.RelativePath;
import hudson.model.ItemGroup;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.util.VariableResolver;
import hudson.util.ListBoxModel;

import java.util.Map;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.ibm.appscan.jenkins.plugin.Messages;
import com.ibm.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.presence.CloudPresenceProvider;

public class DynamicAnalyzer extends Scanner {

	private static final String DYNAMIC_ANALYZER = "Dynamic Analyzer"; //$NON-NLS-1$
	
	private String m_loginUser;
	private Secret m_loginPassword;
	private String m_presenceId;
	private String m_scanFile;
	private String m_testPolicy;
	private String m_scanType;
	private String m_extraField;
	
	public DynamicAnalyzer(String target) {
		this(target, false, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY);
	}
	
	@DataBoundConstructor
	public DynamicAnalyzer(String target, boolean hasOptions, String loginUser, String loginPassword, String presenceId, String scanFile, 
			String testPolicy, String scanType, String extraField) {
		super(target, hasOptions);
		m_loginUser = loginUser;
		m_loginPassword = Secret.fromString(loginPassword);
		m_presenceId = presenceId;
		m_scanFile = scanFile;
		m_testPolicy = EMPTY;
		m_scanType = scanFile != null && !scanFile.equals(EMPTY) ? CUSTOM : scanType;
		m_extraField = extraField;
	}
	
	public String getLoginUser() {
		return m_loginUser;
	}
	
	public String getLoginPassword() {
		return Secret.toString(m_loginPassword);
	}
	
	public String getPresenceId() {
		return m_presenceId;
	}
	
	public String getScanFile() {
		return m_scanFile;
	}
	
	public String getTestPolicy() {
		return m_testPolicy;
	}
	
	public String getScanType() {
		return m_scanType;
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
		Map<String, String> properties = super.getProperties(resolver);
		properties.put(LOGIN_USER, m_loginUser);
		properties.put(LOGIN_PASSWORD, Secret.toString(m_loginPassword));
		properties.put(PRESENCE_ID, m_presenceId);
		properties.put(SCAN_FILE, resolver.resolve(m_scanFile));
		properties.put(TEST_POLICY, m_testPolicy);
		properties.put(SCAN_TYPE, m_scanType);
		properties.put(EXTRA_FIELD, m_extraField);
		return properties;
	}
	
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
    		if(!scanFile.trim().equals(EMPTY) && !scanFile.endsWith(TEMPLATE_EXTENSION)) //$NON-NLS-1$
    			return FormValidation.error(Messages.error_invalid_template_file());
    		return FormValidation.ok();
    	}
    	
    	public FormValidation doCheckTarget(@QueryParameter String target) {
    		return FormValidation.validateRequired(target);
    	}
	}
}

