/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2019, 2023.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.hcl.appscan.jenkins.plugin.builders.AppScanBuildStep;
import com.hcl.appscan.sdk.CoreConstants;
import java.util.HashMap;
import java.util.Map;
import hudson.RelativePath;
import hudson.model.ItemGroup;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.VariableResolver;
import org.kohsuke.stapler.DataBoundSetter;

public class StaticAnalyzer extends Scanner {

	private static final String STATIC_ANALYZER = "Static Analyzer"; //$NON-NLS-1$
        
        private boolean m_includeSCA1;
        private boolean m_includeSCA2;
        private boolean m_sourceCodeOnly;
        private String m_scanMethod;
        private String m_scanSpeed;
        
        @Deprecated
        public StaticAnalyzer(String target){
            this(target,false,false);
        }
        
        public StaticAnalyzer(String target, boolean hasOptions, boolean hasOptions1, boolean includeSCA1, boolean includeSCA2, boolean sourceCodeOnly, String scanMethod, String scanSpeed){
            super(target, hasOptions, hasOptions1);
            m_includeSCA1=includeSCA1;
            m_includeSCA2=includeSCA2;
            m_sourceCodeOnly=sourceCodeOnly;
            m_scanMethod= scanMethod;
            m_scanSpeed=scanSpeed;
        }
        
	@DataBoundConstructor
	public StaticAnalyzer(String target,boolean hasOptions, boolean hasOptions1) {
		super(target, hasOptions, hasOptions1);
                m_includeSCA1=false;
                m_includeSCA2=false;
                m_sourceCodeOnly=false;
                m_scanMethod=CoreConstants.CREATE_IRX;
                m_scanSpeed="";
	}

	@Override
	public String getType() {
		return STATIC_ANALYZER;
	}
  
    	@DataBoundSetter
   	  public void setScanSpeed(String scanSpeed) {
            	m_scanSpeed = scanSpeed;
    	}

    	public String getScanSpeed() {
            if(!m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)){
                return m_scanSpeed;
            }
        	return "";
    	}

    	public String checkScanSpeed(String scanSpeed) {
        	if (m_scanSpeed != null) {
            	return m_scanSpeed.equalsIgnoreCase(scanSpeed) ? "true" : "false";
        		}
        	return null;
    	}
       
        /*public boolean isOpenSourceOnly() {
            if(!m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)){
                return m_openSourceOnly;
            }
            return false;
        }*/
        
        @DataBoundSetter
        public void setIncludeSCA1(boolean includeSCA1) {
            m_includeSCA1 = includeSCA1;
        }

    @DataBoundSetter
    public void setIncludeSCA2(boolean includeSCA2) {
        m_includeSCA2 = includeSCA2;
    }

        public boolean isSourceCodeOnly() {
            if(!m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)){
                return m_sourceCodeOnly;
            }
            return false;
        }

    public boolean isIncludeSCA1() {
        if(!m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)){
            return m_includeSCA1;
        }
        return false;
    }

    public boolean isIncludeSCA2() {
            if(m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)) {
                return m_includeSCA2;
            }
            return false;
    }

        @DataBoundSetter
        public void setSourceCodeOnly(boolean sourceCodeOnly) {
            m_sourceCodeOnly = sourceCodeOnly;
        }

        @DataBoundSetter
        public void setScanMethod(String scanMethod) {
         	m_scanMethod =scanMethod;
        }

        public String getScanMethod() {
        	return m_scanMethod;
    	}

        public boolean isScanMethod(String scanMethod) {
            return m_scanMethod.equals(scanMethod);
        }
	
	public Map<String, String> getProperties(VariableResolver<String> resolver) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(TARGET, resolver == null ? getTarget() : resolvePath(getTarget(), resolver));
                if(getHasOptions()) {
                    properties.put("hasOptions","");
                }
                if(getHasOptions1()) {
                    properties.put("hasOptions1","");
                }
                if (m_scanMethod != null && m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)) {
            		properties.put(CoreConstants.UPLOAD_DIRECT, "");
        	}
        	if ((m_includeSCA1 || m_includeSCA2) && (getHasOptions() || getHasOptions1())) {
                    properties.put(CoreConstants.INCLUDE_SCA, "");
                }
                if (m_sourceCodeOnly && getHasOptions1()) {
                    properties.put(CoreConstants.SOURCE_CODE_ONLY, "");
                }
                if(m_scanSpeed!=null && !m_scanSpeed.isEmpty() && getHasOptions()) {
                    properties.put(SCAN_SPEED, m_scanSpeed);
                }
		return properties;
	}
	
	@Symbol("static_analyzer") //$NON-NLS-1$
	@Extension
	public static final class DescriptorImpl extends ScanDescriptor {
		
		@Override
		public String getDisplayName() {
			return "Static Analysis (SAST)";
		}

		public FormValidation doCheckOpenSourceOnly(@QueryParameter Boolean openSourceOnly, @RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            		JenkinsAuthenticationProvider checkAppScan360Connection = new JenkinsAuthenticationProvider(credentials,context);
			if(openSourceOnly) {
                		if(checkAppScan360Connection.isAppScan360()) {
                    			return FormValidation.error(Messages.error_sca_ui());
                	  	} else {
                    			return FormValidation.warning(Messages.warning_sca_ui());
                		}
            		}
                	return FormValidation.ok();
		}
	}
}
