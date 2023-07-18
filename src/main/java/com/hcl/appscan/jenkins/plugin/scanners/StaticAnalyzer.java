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
import com.hcl.appscan.sdk.scanners.sast.SASTConstants;
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
        private boolean m_sourceCodeOnly;
        private String m_scanMethod;
        
        @Deprecated
        public StaticAnalyzer(String target){
            this(target,false);
        }
        
        public StaticAnalyzer(String target, boolean hasOptions, boolean openSourceOnly, boolean sourceCodeOnly, String scanMethod){
            super(target, hasOptions);
            m_openSourceOnly=openSourceOnly;
            m_sourceCodeOnly=sourceCodeOnly;
            m_scanMethod= scanMethod;
        }
        
	@DataBoundConstructor
	public StaticAnalyzer(String target,boolean hasOptions) {
		super(target, hasOptions);
                m_openSourceOnly=false;
                m_sourceCodeOnly=false;
                m_scanMethod=CoreConstants.CREATE_IRX;
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

        public boolean isSourceCodeOnly() {
            return m_sourceCodeOnly;
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
                if (m_openSourceOnly && getHasOptions()) {
                    properties.put(CoreConstants.OPEN_SOURCE_ONLY, "");
                }
                if (m_sourceCodeOnly && getHasOptions()) {
                    properties.put(CoreConstants.SOURCE_CODE_ONLY, "");
                }
                if (m_scanMethod!=null) {
                    properties.put(CoreConstants.SCAN_METHOD, m_scanMethod);
                }
		return properties;
	}
	
	@Symbol("static_analyzer") //$NON-NLS-1$
	@Extension
	public static final class DescriptorImpl extends ScanDescriptor {
		
		@Override
		public String getDisplayName() {
			return STATIC_ANALYZER;
		}

		public FormValidation doCheckOpenSourceOnly(@QueryParameter Boolean openSourceOnly, @RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context, @QueryParameter String scanMethod) {
            JenkinsAuthenticationProvider checkAppScan360Connection = new JenkinsAuthenticationProvider(credentials,context);
			if((openSourceOnly && checkAppScan360Connection.isAppScan360())) {
                return FormValidation.error(Messages.error_sca_ui());
            }
            return FormValidation.ok();
		}

        public FormValidation doCheckSourceCodeOnly(@QueryParameter String scanMethod,@QueryParameter Boolean additionalOptions, @QueryParameter Boolean sourceCodeOnly){
            if(scanMethod.equals(CoreConstants.UPLOAD_DIRECT) && sourceCodeOnly){
                return FormValidation.error("options are not meant for this scan method");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckAdditionalOptions(@QueryParameter String scanMethod,@QueryParameter Boolean additionalOptions, @QueryParameter Boolean sourceCodeOnly){
            if(scanMethod.equals(CoreConstants.UPLOAD_DIRECT) && additionalOptions){
                return FormValidation.error("options are not meant for this scan method");
            }
            return FormValidation.ok();
        }
	}
}
