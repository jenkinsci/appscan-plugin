package com.ibm.appscan.jenkins.plugin.scanners;

import hudson.Extension;
import hudson.util.Secret;

import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;

public class MobileAnalyzer extends Scanner {

	private static final String MOBILE_ANALYZER = "Mobile Analyzer"; //$NON-NLS-1$
	
	private String m_loginUser;
	private Secret m_loginPassword;
	private String m_extraField;
	private String m_presenceId;
	
	public MobileAnalyzer(String target) {
		this(target, false, EMPTY, EMPTY, EMPTY, EMPTY);
	}
	
	@DataBoundConstructor
	public MobileAnalyzer(String target, boolean hasOptions, String loginUser, String loginPassword, String extraField, String presenceId) {
		super(target, hasOptions);
		m_loginUser = loginUser;
		m_loginPassword = Secret.fromString(loginPassword);
		m_extraField = extraField;
		m_presenceId = presenceId;
	}
	
	public String getLoginUser() {
		return m_loginUser;
	}
	
	public String getLoginPassword() {
		return Secret.toString(m_loginPassword);
	}
	
	public String getExtraField() {
		return m_extraField;
	}

	public String getPresenceId() {
		return m_presenceId;
	}
	
	@Override
	public String getType() {
		return MOBILE_ANALYZER;
	}
	
	@Override
	public Map<String, String> getProperties() {
		Map<String, String> properties = super.getProperties();
		properties.put(LOGIN_USER, m_loginUser);
		properties.put(LOGIN_PASSWORD, Secret.toString(m_loginPassword));
		properties.put(EXTRA_FIELD, m_extraField);
		properties.put(PRESENCE_ID, m_presenceId);
		return properties;
	}

	@Extension
	public static final class DescriptorImpl extends ScanDescriptor {
		
		@Override
		public String getDisplayName() {
			return MOBILE_ANALYZER;
		}
	}
}
