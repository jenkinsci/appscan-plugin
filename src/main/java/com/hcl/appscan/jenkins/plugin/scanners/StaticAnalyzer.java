/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2019, 2023.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
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
        
        private boolean m_openSourceOnly;
        private String m_includeSCAGenerateIRX;
        private boolean m_includeSCAUploadDirect;
        private boolean m_sourceCodeOnly;
        private String m_scanMethod;
        private String m_scanSpeed;
        
        @Deprecated
        public StaticAnalyzer(String target){
            this(target,true, false, false, EMPTY, EMPTY, false, EMPTY, false);
        }
        
        public StaticAnalyzer(String target, boolean hasOptions, boolean openSourceOnly, boolean sourceCodeOnly, String scanMethod, String scanSpeed, boolean hasOptionsUploadDirect, String includeSCAGenerateIRX, boolean includeSCAUploadDirect){
            super(target, hasOptions, hasOptionsUploadDirect);
            m_openSourceOnly = openSourceOnly;
            m_sourceCodeOnly=sourceCodeOnly;
            m_scanMethod= scanMethod;
            m_scanSpeed=scanSpeed;
            m_includeSCAGenerateIRX=includeSCAGenerateIRX;
            m_includeSCAUploadDirect=includeSCAUploadDirect;
        }
        
	@DataBoundConstructor
	public StaticAnalyzer(String target,boolean hasOptions, boolean hasOptionsUploadDirect) {
		super(target, hasOptions, hasOptionsUploadDirect);
                m_openSourceOnly=false;
                m_sourceCodeOnly=false;
                m_scanMethod=CoreConstants.CREATE_IRX;
                m_scanSpeed="";
                m_includeSCAGenerateIRX="true";
                m_includeSCAUploadDirect=false;
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

        @DataBoundSetter
        public void setOpenSourceOnly(boolean openSourceOnly) {
            m_openSourceOnly = openSourceOnly;
        }

        public boolean getOpenSourceOnly() {
            return m_openSourceOnly;
        }
        
        @DataBoundSetter
        public void setIncludeSCAGenerateIRX(String includeSCAGenerateIRX) {
            m_includeSCAGenerateIRX = includeSCAGenerateIRX;
        }

        public String getIncludeSCAGenerateIRX() {
            return m_includeSCAGenerateIRX;
        }

        public String isIncludeSCAGenerateIRX(String includeSCAGenerateIRX) {
            return m_includeSCAGenerateIRX;
        }

        @DataBoundSetter
        public void setIncludeSCAUploadDirect(boolean includeSCAUploadDirect) {
            m_includeSCAUploadDirect = includeSCAUploadDirect;
        }

        public boolean isIncludeSCAUploadDirect() {
            if(m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)) {
                return m_includeSCAUploadDirect;
            }
            return false;
        }

        @DataBoundSetter
        public void setSourceCodeOnly(boolean sourceCodeOnly) {
            m_sourceCodeOnly = sourceCodeOnly;
        }

        public boolean isSourceCodeOnly() {
            if(!m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)){
                return m_sourceCodeOnly;
            }
            return false;
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
                if (m_scanMethod != null && m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)) {
            		properties.put(CoreConstants.UPLOAD_DIRECT, "");
                }
                if (m_openSourceOnly && getHasOptions()) {
                    properties.put(CoreConstants.OPEN_SOURCE_ONLY, "");
                }
                if ((m_includeSCAGenerateIRX == null || (m_includeSCAGenerateIRX.equals("true")  && getHasOptions() && m_scanMethod.equals(CoreConstants.CREATE_IRX)) || (m_includeSCAUploadDirect && getHasOptionsUploadDirect() && m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)))) {
                    properties.put(CoreConstants.INCLUDE_SCA, "");
                }
                if (m_sourceCodeOnly && getHasOptions()) {
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

        public FormValidation doCheckIncludeSCAGenerateIRX(@QueryParameter Boolean includeSCAGenerateIRX, @RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            JenkinsAuthenticationProvider checkAppScan360Connection = new JenkinsAuthenticationProvider(credentials, context);
            if (includeSCAGenerateIRX && checkAppScan360Connection.isAppScan360()) {
                    return FormValidation.error(Messages.error_include_sca_ui());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckIncludeSCAUploadDirect(@QueryParameter Boolean includeSCAUploadDirect, @QueryParameter String target, @RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            JenkinsAuthenticationProvider checkAppScan360Connection = new JenkinsAuthenticationProvider(credentials, context);
            if (includeSCAUploadDirect && checkAppScan360Connection.isAppScan360()) {
                    return FormValidation.error(Messages.error_include_sca_ui());
            }
            return FormValidation.ok();
        }
	}
}
