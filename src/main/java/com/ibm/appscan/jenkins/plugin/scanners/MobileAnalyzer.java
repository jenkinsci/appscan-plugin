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

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.presence.CloudPresenceProvider;
import com.ibm.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;

public class MobileAnalyzer extends Scanner {

	private static final String MOBILE_ANALYZER = "Mobile Analyzer"; //$NON-NLS-1$
	
	private String m_loginUser;
	private Secret m_loginPassword;
	private String m_extraField;
	private String m_presenceId;
	
	@Deprecated
	public MobileAnalyzer(String target) {
		this(target, false, EMPTY, EMPTY, EMPTY, EMPTY);
	}
	
	@Deprecated
	public MobileAnalyzer(String target, boolean hasOptions, String loginUser, String loginPassword, String extraField, String presenceId) {
		super(target, hasOptions);
		m_loginUser = loginUser;
		m_loginPassword = Secret.fromString(loginPassword);
		m_extraField = extraField;
		m_presenceId = presenceId;
	}
	
	@DataBoundConstructor
	public MobileAnalyzer(String target, boolean hasOptions) {
		super(target, hasOptions);
		m_loginUser = EMPTY;
		m_loginPassword = Secret.fromString(EMPTY);
		m_extraField = EMPTY;
		m_presenceId = EMPTY;
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
	public void setExtraField(String extraField) {
		m_extraField = extraField;
	}
	
	public String getExtraField() {
		return m_extraField;
	}

	@DataBoundSetter
	public void setPresenceId(String presenceId) {
		m_presenceId = presenceId;
	}
	
	public String getPresenceId() {
		return m_presenceId;
	}
	
	@Override
	public String getType() {
		return MOBILE_ANALYZER;
	}
	
	@Override
	public Map<String, String> getProperties(VariableResolver<String> resolver) {
		Map<String, String> properties = super.getProperties(resolver);
		properties.put(LOGIN_USER, m_loginUser);
		properties.put(LOGIN_PASSWORD, Secret.toString(m_loginPassword));
		properties.put(EXTRA_FIELD, m_extraField);
		properties.put(PRESENCE_ID, m_presenceId);
		return properties;
	}

	@Symbol("mobile-analyzer") //$NON-NLS-1$
	@Extension
	public static final class DescriptorImpl extends ScanDescriptor {
		
		@Override
		public String getDisplayName() {
			return MOBILE_ANALYZER;
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
		
    	public FormValidation doCheckTarget(@QueryParameter String target) {
    		return FormValidation.validateRequired(target);
    	}
	}
}
