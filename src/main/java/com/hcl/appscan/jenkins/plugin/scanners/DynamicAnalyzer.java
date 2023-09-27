/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2022, 2023.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.hcl.appscan.jenkins.plugin.auth.ASoCCredentials;
import com.hcl.appscan.jenkins.plugin.builders.AppScanBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.presence.CloudPresenceProvider;
import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;

import hudson.AbortException;
import hudson.Extension;
import hudson.RelativePath;
import hudson.Util;
import hudson.model.ItemGroup;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import hudson.util.VariableResolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DynamicAnalyzer extends Scanner {

	private static final String DYNAMIC_ANALYZER = "Dynamic Analyzer"; //$NON-NLS-1$

	private String m_presenceId;
	private String m_scanFile;
	private String m_scanType;
	private String m_optimization;
	private String m_extraField;
	private String m_loginType;
	private String m_loginUser;
	private Secret m_loginPassword;
	private String m_trafficFile;

	@Deprecated
	public DynamicAnalyzer(String target) {
		this(target, false, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY);
	}

	@Deprecated
	public DynamicAnalyzer(String target, boolean hasOptions, String presenceId, String scanFile, String scanType, String optimization, String extraField, String loginUser, String loginPassword, String trafficFile, String loginType) {
		super(target, hasOptions);
		m_presenceId = presenceId;
		m_scanFile = scanFile;
		m_scanType = scanFile != null && !scanFile.equals(EMPTY) ? CUSTOM : scanType;
		m_optimization = optimization;
		m_extraField = extraField;
		m_loginUser = loginUser;
		m_loginPassword = Secret.fromString(loginPassword);
		m_trafficFile = trafficFile;
		m_loginType = loginType;
	}

	@DataBoundConstructor

	public DynamicAnalyzer(String target, boolean hasOptions) {
		super(target, hasOptions);
		m_presenceId = EMPTY;
		m_scanFile = EMPTY;
		m_scanType = EMPTY;
		m_optimization = EMPTY;
		m_extraField = EMPTY;
		m_loginType = EMPTY;
		m_loginUser = EMPTY;
		m_loginPassword = Secret.fromString(EMPTY);
		m_trafficFile = EMPTY;
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
	public void setScanType(String scanType) {
		m_scanType = m_scanFile != null && !m_scanFile.equals(EMPTY) ? CUSTOM : scanType;
	}

	public String getScanType() {
		return m_scanType;
	}

	@DataBoundSetter
	public void setOptimization(String optimization) {
        m_optimization = (optimization != null) ? mapOldtoNewOptLevels(optimization) : null;
	}

	public String getOptimization() {
		m_optimization = mapOldtoNewOptLevels(m_optimization);
		return m_optimization;
	}

	@DataBoundSetter
	public void setExtraField(String extraField) {
		m_extraField = extraField;
	}

	public String getExtraField() {
		return m_extraField;
	}

	@DataBoundSetter
	public void setLoginType(String loginType) {
			m_loginType =loginType;
	}

	public String getLoginType() {
		return m_loginType;
	}

	@DataBoundSetter
	public void setTrafficFile(String trafficFile) {
		if (RECORDED.equals(m_loginType))
			m_trafficFile = trafficFile;
	}

	public String getTrafficFile() {
		return m_trafficFile;
	}

	@Override
	public String getType() {
		return DYNAMIC_ANALYZER;
	}

	public String isLoginTypes(String loginTypeName) {
		if (m_loginType != null) {
			return m_loginType.equalsIgnoreCase(loginTypeName) ? "true" : "";
		} else if(!(((m_loginUser.equals(""))) && m_loginPassword.equals(Secret.fromString("")))){
			m_loginType = AUTOMATIC;
			return "true";
		} else if (loginTypeName.equals(NONE)) { //Default
			return "true";
		} else {
			return "";
		}
	}

	public String upgradeLoginScenario(){
		if(!(((m_loginUser.equals(""))) || m_loginPassword.equals(Secret.fromString("")))){
			return m_loginType = AUTOMATIC;
		} else {
			return m_loginType = NONE;
		}
	}

	@Override
	public Map<String, String> getProperties(VariableResolver<String> resolver) throws hudson.AbortException {
		Map<String, String> properties = new HashMap<String, String>();
		if (resolver == null) {
			properties.put(TARGET, getTarget());
			properties.put(SCAN_FILE, m_scanFile);
			properties.put(EXTRA_FIELD, m_extraField);
			if(m_loginType == null || m_loginType.equals("")){
				m_loginType = upgradeLoginScenario();
			}
				if (RECORDED.equals(m_loginType)) {
					properties.put(TRAFFIC_FILE, m_trafficFile);
					if (m_trafficFile == null || m_trafficFile.equals("")) {
						throw new hudson.AbortException(Messages.error_login_fields_empty_manual());
					} else if ((!((m_trafficFile).toLowerCase().endsWith(TEMPLATE_EXTENSION3)))){
						throw new hudson.AbortException(Messages.error_login_fields_manual());
					}
				} else if (AUTOMATIC.equals(m_loginType)) {
					properties.put(LOGIN_USER, m_loginUser);
					properties.put(LOGIN_PASSWORD, Secret.toString(m_loginPassword));
					if(m_loginUser.equals("") || m_loginPassword.equals(Secret.fromString(""))){
						throw new hudson.AbortException(Messages.error_login_fields_automatic());
					}
				}
		} else {
			properties.put(TARGET, Util.replaceMacro(getTarget(), resolver));
			properties.put(SCAN_FILE, m_scanFile.equals("") ? m_scanFile : resolvePath(m_scanFile, resolver));
			properties.put(EXTRA_FIELD, Util.replaceMacro(m_extraField, resolver));
			if(m_loginType == null || m_loginType.equals("")){
				m_loginType = upgradeLoginScenario();
			}
			if (RECORDED.equals(m_loginType)) {
				properties.put(TRAFFIC_FILE, resolvePath(m_trafficFile, resolver));
					if (m_trafficFile == null || m_trafficFile.equals("")) {
						throw new hudson.AbortException(Messages.error_login_fields_empty_manual());
					} else if((!((resolvePath(m_trafficFile, resolver)).toLowerCase().endsWith(TEMPLATE_EXTENSION3)))){
						throw new hudson.AbortException(Messages.error_login_fields_manual());
					}
			} else if (AUTOMATIC.equals(m_loginType)) {
				properties.put(LOGIN_USER, Util.replaceMacro(m_loginUser, resolver));
				properties.put(LOGIN_PASSWORD, Util.replaceMacro(Secret.toString(m_loginPassword), resolver));
					if (m_loginUser.equals("") || m_loginPassword.equals(Secret.fromString(""))) {
						throw new AbortException(Messages.error_login_fields_automatic());
					}
			}
		}
		properties.put(LOGIN_TYPE,m_loginType);
		properties.put(SCAN_TYPE, m_scanType);
		properties.put(TEST_OPTIMIZATION_LEVEL, m_optimization);
		properties.put(PRESENCE_ID, m_presenceId);

		return properties;
	}

	private String mapOldtoNewOptLevels(String optimization) //Backward Compatibility
	{
		if(optimization != null) {
			if(optimization.equals(NORMAL)) {
				m_optimization = NO_OPTIMIZATION;
			} else if(optimization.equals(OPTIMIZED)) {
				m_optimization = FAST;
			} else {
				m_optimization = optimization;
			}
		}
		return m_optimization;
	}

	@Symbol("dynamic_analyzer") //$NON-NLS-1$
	@Extension
	public static final class DescriptorImpl extends ScanDescriptor {

		@Override
		public String getDisplayName() {
			return "Dynamic Analysis (DAST)";
		}

		public ListBoxModel doFillScanTypeItems() {
			ListBoxModel model = new ListBoxModel();
			model.add(Messages.option_staging(), STAGING);
			model.add(Messages.option_production(), PRODUCTION);
			return model;
		}

		public ListBoxModel doFillOptimizationItems() {
			ListBoxModel model = new ListBoxModel();
			model.add(Messages.option_fast(), FAST);
			model.add(Messages.option_faster(), FASTER);
			model.add(Messages.option_fastest(), FASTEST);
			model.add(Messages.option_nooptimization(), NO_OPTIMIZATION);
			return model;
		}

		public ListBoxModel doFillPresenceIdItems(@RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) { //$NON-NLS-1$
			IAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials, context);
			Map<String, String> presences = new CloudPresenceProvider(authProvider).getPresences();
			ListBoxModel model = new ListBoxModel();
			model.add(""); //$NON-NLS-1$

			if (presences != null) {
				List<Map.Entry<String, String>> list = sortPresences(presences.entrySet());

				for (Map.Entry<String, String> entry : list)
					model.add(entry.getValue(), entry.getKey());
			}
			return model;
		}

		private List<Map.Entry<String, String>> sortPresences(Set<Map.Entry<String, String>> set) {
			List<Map.Entry<String, String>> list = new ArrayList<>(set);
			if (list.size() > 1) {
				Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
					public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
						return (o1.getValue().toLowerCase()).compareTo(o2.getValue().toLowerCase());
					}
				});
			}
			return list;
		}

		public FormValidation doCheckScanFile(@QueryParameter String scanFile) {
			if (!scanFile.trim().equals(EMPTY) && !scanFile.endsWith(TEMPLATE_EXTENSION) && !scanFile.endsWith(TEMPLATE_EXTENSION2) && !scanFile.startsWith("${"))
				return FormValidation.error(Messages.error_invalid_template_file());
			return FormValidation.ok();
		}

		public FormValidation doCheckTarget(@QueryParameter String target,@RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
			JenkinsAuthenticationProvider checkAppScan360Connection = new JenkinsAuthenticationProvider(credentials,context);
			if(checkAppScan360Connection.isAppScan360()){
				return FormValidation.error(Messages.error_dynamic_AppScan360());
			}
			return FormValidation.validateRequired(target);
		}

		public FormValidation doCheckLoginUser(@QueryParameter String loginUser) {
			return FormValidation.validateRequired(loginUser);
		}

		public FormValidation doCheckLoginPassword(@QueryParameter String loginPassword) {
			return FormValidation.validateRequired(loginPassword);
		}

		public FormValidation doCheckTrafficFile(@QueryParameter String trafficFile) {
			if (trafficFile.trim().equals(EMPTY))
				return FormValidation.validateRequired(trafficFile);
			if (!trafficFile.toLowerCase().endsWith(TEMPLATE_EXTENSION3) && !trafficFile.startsWith("${"))
				return FormValidation.error(Messages.error_invalid_login_sequence_file());
			return  FormValidation.ok();
		}
	}
}

