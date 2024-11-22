/**
 * @ Copyright HCL Technologies Ltd. 2023, 2024.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.utils.ServiceUtil;
import hudson.AbortException;
import hudson.Extension;
import hudson.RelativePath;
import hudson.model.ItemGroup;
import hudson.util.FormValidation;
import hudson.util.VariableResolver;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.HashMap;
import java.util.Map;

public class SoftwareCompositionAnalyzer extends Scanner {

    private boolean m_rescan;
    private String m_scanId;

    @Deprecated
    public SoftwareCompositionAnalyzer(String target){
        super(target, false);
    }

    public SoftwareCompositionAnalyzer(String target, boolean rescan, String scanId) {
        super(target, false);
        m_rescan = rescan;
        m_scanId = scanId;
    }

    @DataBoundConstructor
    public SoftwareCompositionAnalyzer(String target, boolean hasOptions){
        super(target, hasOptions);
        m_rescan = false;
        m_scanId = EMPTY;
    }


    @Override
    public String getType() {
        return SOFTWARE_COMPOSITION_ANALYZER;
    }

    @DataBoundSetter
    public void setRescan(boolean rescan) {
        m_rescan = rescan;
    }

    public boolean getRescan() {
        return m_rescan;
    }

    @DataBoundSetter
    public void setScanId(String scanId) {
        m_scanId = scanId;
    }
    public String getScanId() {
        return m_scanId;
    }

    public void validateSettings(JenkinsAuthenticationProvider authProvider, Map<String, String> properties, IProgress progress) throws AbortException {
        if(!ServiceUtil.hasScaEntitlement(authProvider)) {
            throw new AbortException(Messages.error_active_subscription_validation(getType()));
        }

        if (authProvider.isAppScan360()) {
            throw new AbortException(Messages.error_sca_AppScan360());
        }
    }

    public Map<String, String> getProperties(VariableResolver<String> resolver) throws AbortException {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(TARGET, resolver == null ? getTarget() : resolvePath(getTarget(), resolver));
        if(getRescan() && isNullOrEmpty(getScanId())) {
            properties.put(CoreConstants.SCAN_ID,getScanId());
        }
        return properties;
    }

    @Symbol("software_composition_analysis") //$NON-NLS-1$
    @Extension
    public static final class DescriptorImpl extends ScanDescriptor {

        @Override
        public String getDisplayName() {
            return "Software Composition Analysis (SCA)";
        }

        public FormValidation doCheckScanId(@QueryParameter String scanId, @RelativePath("..") @QueryParameter String application, @RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) throws JSONException {
            JenkinsAuthenticationProvider provider = new JenkinsAuthenticationProvider(credentials, context);
            if(scanId!=null && !scanId.isEmpty()) {
                JSONObject scanDetails = ServiceUtil.scanSpecificDetails(SOFTWARE_COMPOSITION_ANALYZER, scanId, provider);
                if(scanDetails == null) {
                    return FormValidation.error(Messages.error_invalid_scan_id_ui());
                } else if (!scanDetails.get("RescanAllowed").equals(true)) {
                    return FormValidation.error(Messages.error_invalid_scan_id_rescan_allowed_ui());
                } else if (!scanDetails.get(CoreConstants.APP_ID).equals(application)) {
                    return FormValidation.error(Messages.error_invalid_scan_id_application_ui());
                }
            }
            return FormValidation.validateRequired(scanId);
        }

        public FormValidation doCheckTarget(@RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            JenkinsAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials,context);
            if(!ServiceUtil.hasScaEntitlement(authProvider)) {
                return FormValidation.error(Messages.error_active_subscription_validation_ui());
            }
            if(authProvider.isAppScan360()){
                return FormValidation.error(Messages.error_sca_AppScan360_ui());
            }
            return FormValidation.ok();
        }
    }
}
