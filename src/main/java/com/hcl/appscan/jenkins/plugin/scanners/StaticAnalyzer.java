/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2019, 2024, 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.hcl.appscan.sdk.CoreConstants;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.logging.Message;
import com.hcl.appscan.sdk.scan.CloudScanServiceProvider;
import com.hcl.appscan.sdk.utils.ServiceUtil;
import hudson.AbortException;
import hudson.RelativePath;
import hudson.model.ItemGroup;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
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
        private boolean m_hasOptionsUploadDirect;
        private String m_includeSCAUploadDirect;
        private boolean m_sourceCodeOnly;
        private String m_scanMethod;
        private String m_scanSpeed;
        
        @Deprecated
        public StaticAnalyzer(String target) {
            this(target, true);
        }
        
        public StaticAnalyzer(String target, boolean hasOptions, boolean rescan, String scanId, boolean openSourceOnly, boolean sourceCodeOnly, String scanMethod, String scanSpeed, String includeSCAGenerateIRX, boolean hasOptionsUploadDirect, String includeSCAUploadDirect){
            super(target, hasOptions, rescan, scanId);
            m_openSourceOnly=openSourceOnly;
            m_sourceCodeOnly=sourceCodeOnly;
            m_scanMethod= scanMethod;
            m_scanSpeed=scanSpeed;
            m_includeSCAGenerateIRX=includeSCAGenerateIRX == null ? Boolean.toString(true): includeSCAGenerateIRX;
            m_hasOptionsUploadDirect=hasOptionsUploadDirect;
            m_includeSCAUploadDirect=includeSCAUploadDirect;
        }
        
	@DataBoundConstructor
	public StaticAnalyzer(String target,boolean hasOptions) {
            super(target, hasOptions, false, EMPTY);
            m_openSourceOnly=false;
            m_sourceCodeOnly=false;
            m_scanMethod=CoreConstants.CREATE_IRX;
            m_scanSpeed="";
            m_includeSCAGenerateIRX=Boolean.toString(true);
            m_hasOptionsUploadDirect=false;
            m_includeSCAUploadDirect=Boolean.toString(false);
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
            if(getHasOptions() && Boolean.parseBoolean(includeSCAGenerateIRX)) {
                m_includeSCAGenerateIRX = Boolean.toString(true);
            } else {
                m_includeSCAGenerateIRX = Boolean.toString(false);
            }
        }

        public String getIncludeSCAGenerateIRX() {
            if(!m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)){
                return m_includeSCAGenerateIRX;
            }
            return "";
        }

        //using this method in the jelly file to determine the checkbox value
        public String isIncludeSCAGenerateIRX(String includeSCAGenerateIRX) {
            if (m_includeSCAGenerateIRX != null) {
                return m_includeSCAGenerateIRX.equalsIgnoreCase(includeSCAGenerateIRX) ? Boolean.toString(true) : Boolean.toString(false);
            }
            return Boolean.toString(true);
        }

        @DataBoundSetter
        public void setHasOptionsUploadDirect(boolean hasOptionsUploadDirect) {
            m_hasOptionsUploadDirect = hasOptionsUploadDirect;
        }

        //using this method in the jelly file to determine the checkbox value
        public boolean getHasOptionsUploadDirect() {
            return m_hasOptionsUploadDirect;
        }

        @DataBoundSetter
        public void setIncludeSCAUploadDirect(String includeSCAUploadDirect) {
            if(m_hasOptionsUploadDirect && Boolean.parseBoolean(includeSCAUploadDirect)) {
                m_includeSCAUploadDirect = Boolean.toString(true);
            } else {
                m_includeSCAUploadDirect = Boolean.toString(false);
            }
        }

        public String getIncludeSCAUploadDirect() {
            if(m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)){
                return m_includeSCAUploadDirect;
            }
            return "";
        }

        //using this method in the jelly file to determine the checkbox value
        public String isIncludeSCAUploadDirect(String includeSCAUploadDirect) {
            if (m_includeSCAUploadDirect != null) {
                return m_includeSCAUploadDirect.equalsIgnoreCase(includeSCAUploadDirect) ? Boolean.toString(true) : Boolean.toString(false);
            }
            return Boolean.toString(false);
        }

        @DataBoundSetter
        public void setSourceCodeOnly(boolean sourceCodeOnly) {
            m_sourceCodeOnly = sourceCodeOnly && getHasOptions();
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

        @Override
        public void validateSettings(JenkinsAuthenticationProvider authProvider, Map<String, String> properties, IProgress progress, boolean isAppScan360) throws IOException {
            super.validateSettings(authProvider, properties, progress, isAppScan360);
            if(!ServiceUtil.hasSastEntitlement(authProvider)) {
                throw new AbortException(Messages.error_active_subscription_validation(getType()));
            }
          
           if(getRescan() && !properties.containsKey(CoreConstants.SCAN_ID)) {
                throw new AbortException(Messages.error_empty_scan_id());
            } else if(properties.containsKey(CoreConstants.SCAN_ID)) {
               properties.remove(CoreConstants.INCLUDE_SCA);
           }

            // Handle AppScan360-specific validation
            if (authProvider.isAppScan360()) {
                if (properties.containsKey(CoreConstants.OPEN_SOURCE_ONLY)) {
                    throw new AbortException(Messages.error_sca_AppScan360());
                }
                // Remove INCLUDE_SCA and show warning if present in AppScan360 context
                if (properties.containsKey(CoreConstants.INCLUDE_SCA)) {
                    progress.setStatus(new Message(Message.WARNING, Messages.warning_include_sca_AppScan360()));
                    properties.remove(CoreConstants.INCLUDE_SCA);
                }
            } else {
                // Handle SCA entitlement validation and removal of INCLUDE_SCA if no entitlement
                if (properties.containsKey(CoreConstants.INCLUDE_SCA) && !ServiceUtil.hasScaEntitlement(authProvider)) {
                    progress.setStatus(new Message(Message.WARNING, Messages.warning_sca_subscription()));
                    properties.remove(CoreConstants.INCLUDE_SCA);
                }
            }

            //includeSCA is only available if the user upload an IRX file for upload files & folders scan method .
            if (properties.containsKey(CoreConstants.INCLUDE_SCA) && properties.containsKey(CoreConstants.UPLOAD_DIRECT) && !properties.get(TARGET).endsWith(".irx")) {
                throw new AbortException(Messages.error_invalid_format_include_sca());
            }

            if(properties.containsKey(CoreConstants.SCAN_ID)) {
                try {
                    JSONObject scanDetails = new CloudScanServiceProvider(authProvider).getScanDetails(STATIC_ANALYZER, properties.get(CoreConstants.SCAN_ID));;
                    if(scanDetails!=null && scanDetails.containsKey("GitRepoPlatform") && scanDetails.get("GitRepoPlatform")!=null) {
                        throw new AbortException(Messages.error_invalid_scan_id_git_repo());
                    }
                } catch (JSONException e) {
                    //Ignore and move on.
                }
            }
        }

        @Override
        public Map<String,String> getProperties(VariableResolver<String> resolver) {
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(TARGET, resolver == null ? getTarget() : resolvePath(getTarget(), resolver));
            if (m_scanMethod != null && m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)) {
                properties.put(CoreConstants.UPLOAD_DIRECT, "");
            }
            if (m_openSourceOnly && getHasOptions()) {
                properties.put(CoreConstants.OPEN_SOURCE_ONLY, "");
            }
            if (m_sourceCodeOnly && !m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT)) {
                properties.put(CoreConstants.SOURCE_CODE_ONLY, "");
            }
            if ((m_scanMethod.equals(CoreConstants.CREATE_IRX) && (m_includeSCAGenerateIRX == null || Boolean.parseBoolean(m_includeSCAGenerateIRX))) || (m_scanMethod.equals(CoreConstants.UPLOAD_DIRECT) && Boolean.parseBoolean(m_includeSCAUploadDirect))) {
                properties.put(CoreConstants.INCLUDE_SCA, "");
            }
            if(isNullOrEmpty(m_scanSpeed) && getHasOptions()) {
                properties.put(SCAN_SPEED, m_scanSpeed);
            }
            if(getRescan() && isNullOrEmpty(getScanId()) ){
                properties.put(CoreConstants.SCAN_ID,getScanId());
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

        public FormValidation doCheckIncludeSCAGenerateIRX(@QueryParameter String includeSCAGenerateIRX, @QueryParameter boolean rescan, @RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            JenkinsAuthenticationProvider checkAppScan360Connection = new JenkinsAuthenticationProvider(credentials, context);
            if (!rescan && Boolean.parseBoolean(includeSCAGenerateIRX) && checkAppScan360Connection.isAppScan360()) {
                    return FormValidation.error(Messages.error_include_sca_ui());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckScanId(@QueryParameter String scanId, @RelativePath("..") @QueryParameter String application, @RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            if (scanId == null || scanId.isEmpty()) {
                    return FormValidation.validateRequired(scanId);
            }

            JenkinsAuthenticationProvider provider = new JenkinsAuthenticationProvider(credentials, context);
            JSONObject scanDetails = new CloudScanServiceProvider(provider).getScanDetails(STATIC_ANALYZER, scanId);

            if (isGitRepoPlatform(scanDetails)) {
                    return FormValidation.error(Messages.error_invalid_scan_id_git_repo_ui());
            }

            try {
                    return scanIdValidation(scanDetails, application);
            } catch (JSONException e) {
                    // Ignore and move on
            }

            return FormValidation.ok();
        }

        // Helper method to check if the scan details contain a GitRepoPlatform
        private boolean isGitRepoPlatform(JSONObject scanDetails) {
            try {
                    return scanDetails != null && scanDetails.containsKey("GitRepoPlatform") && scanDetails.get("GitRepoPlatform") != null;
            } catch (JSONException e) {
                    // Ignore and move on
            }
            return false;
        }


        public FormValidation doCheckIncludeSCAUploadDirect(@QueryParameter String includeSCAUploadDirect, @RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            JenkinsAuthenticationProvider checkAppScan360Connection = new JenkinsAuthenticationProvider(credentials, context);
            if (Boolean.parseBoolean(includeSCAUploadDirect) && checkAppScan360Connection.isAppScan360()) {
                    return FormValidation.error(Messages.error_include_sca_ui());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckTarget(@RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            JenkinsAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials,context);
            if(!ServiceUtil.hasSastEntitlement(authProvider)) {
                    return FormValidation.error(Messages.error_active_subscription_validation_ui());
            }
            return FormValidation.ok();
        }
	}
}
