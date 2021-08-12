/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2019, 2021.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import com.hcl.appscan.sdk.CoreConstants;
import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.VariableResolver;
import org.kohsuke.stapler.DataBoundSetter;

public class StaticAnalyzer extends Scanner {

	private static final String STATIC_ANALYZER = "Static Analyzer"; //$NON-NLS-1$
        
        private boolean m_openSourceOnly;
        
        @Deprecated
        public StaticAnalyzer(String target){
            this(target,false);
        }
        
        public StaticAnalyzer(String target, boolean hasOptions, boolean openSourceOnly){
            super(target, hasOptions);
            m_openSourceOnly=openSourceOnly;
        }
        
	@DataBoundConstructor
	public StaticAnalyzer(String target,boolean hasOptions) {
		super(target, hasOptions);
                m_openSourceOnly=false;
	}

	@Override
	public String getType() {
		return STATIC_ANALYZER;
	}
        
        public boolean isOpenSourceOnly() {
            return m_openSourceOnly;
        }
        
        @DataBoundSetter
        public void setOpenSourceOnly(boolean openSourceOnly) {
            m_openSourceOnly = openSourceOnly;
        }
	
        @Override
	public Map<String, String> getProperties(VariableResolver<String> resolver) {
		Map<String, String> properties = new HashMap();
		properties.put(TARGET, resolver == null ? getTarget() : resolvePath(getTarget(), resolver));
                if (m_openSourceOnly)
                    properties.put(CoreConstants.OPEN_SOURCE_ONLY, "");
		return properties;
	}
	
	@Symbol("static_analyzer") //$NON-NLS-1$
	@Extension
	public static final class DescriptorImpl extends ScanDescriptor {
		
		@Override
		public String getDisplayName() {
			return STATIC_ANALYZER;
		}
			
	    	public FormValidation doCheckTarget(@QueryParameter String target) {
	    		return FormValidation.validateRequired(target);
	    	}
	}
}
