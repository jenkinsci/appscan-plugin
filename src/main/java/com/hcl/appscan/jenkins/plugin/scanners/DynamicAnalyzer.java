/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2020, 2021.
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
	
	private String m_loginUser;
	private Secret m_loginPassword;
	private String m_presenceId;
	private String m_scanFile;
	private String m_scanType;
	private String m_optimization;
	private String m_extraField;
	
	@Deprecated
	public DynamicAnalyzer(String target) {
		this(target, false, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY); 
	}
	
	@Deprecated
	public DynamicAnalyzer(String target, boolean hasOptions, String loginUser, String loginPassword, String presenceId, String scanFile, 
			String scanType,String optimization, String extraField) {
		super(target, hasOptions);
		m_loginUser = loginUser;
		m_loginPassword = Secret.fromString(loginPassword);
		m_presenceId = presenceId;
		m_scanFile = scanFile;
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
	
	public Secret getLoginPassword() {
		return m_loginPassword;
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
		if(optimization != null) {
			m_optimization = mapOldtoNewOptLevels(optimization);
		} else {
			m_optimization = optimization;
		}
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
	
	@Override
	public String getType() {
		return DYNAMIC_ANALYZER;
	}
	
	@Override
	public Map<String, String> getProperties(VariableResolver<String> resolver) {
		Map<String, String> properties = new HashMap<String, String>();
                if(resolver == null) {
			properties.put(TARGET, getTarget());
			properties.put(LOGIN_USER, m_loginUser);
			properties.put(LOGIN_PASSWORD, Secret.toString(m_loginPassword));
			properties.put(SCAN_FILE, m_scanFile);
			properties.put(EXTRA_FIELD, m_extraField);
                }
                else {
			properties.put(TARGET, Util.replaceMacro(getTarget(), resolver));
			properties.put(LOGIN_USER, Util.replaceMacro(m_loginUser, resolver));
			properties.put(LOGIN_PASSWORD, Util.replaceMacro(Secret.toString(m_loginPassword), resolver));
			properties.put(SCAN_FILE, resolvePath(m_scanFile, resolver));
			properties.put(EXTRA_FIELD, Util.replaceMacro(m_extraField, resolver));
                }
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
    		
    		if(presences != null) {
                    List<Map.Entry<String , String>> list=sortPresences(presences.entrySet());
                    
	    		for(Map.Entry<String, String> entry : list)
	    			model.add(entry.getValue(), entry.getKey());
    		}
    		return model;
    	}
        
        private List<Map.Entry<String , String>> sortPresences(Set<Map.Entry<String , String>> set) {
    		List<Map.Entry<String , String>> list= new ArrayList<>(set);
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
		
    	public FormValidation doCheckScanFile(@QueryParameter String scanFile) {
    		if(!scanFile.trim().equals(EMPTY) && !scanFile.endsWith(TEMPLATE_EXTENSION) && !scanFile.endsWith(TEMPLATE_EXTENSION2) && !scanFile.startsWith("${") )
    			return FormValidation.error(Messages.error_invalid_template_file());
    		return FormValidation.ok();
    	}
    	
    	public FormValidation doCheckTarget(@QueryParameter String target) {
    		return FormValidation.validateRequired(target);
    	}
	}
}

