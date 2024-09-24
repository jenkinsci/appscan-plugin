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
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.HashMap;
import java.util.Map;

public class SoftwareCompositionAnalyzer extends Scanner {

    @Deprecated
    public SoftwareCompositionAnalyzer(String target){
        super(target, false);
    }

    public SoftwareCompositionAnalyzer(String target, boolean rescan, String scanId){
        super(target, false, rescan, scanId);
    }

    @DataBoundConstructor
    public SoftwareCompositionAnalyzer(String target, boolean hasOptions, boolean rescan, String scanId){
        super(target, false, rescan, scanId);
    }


    @Override
    public String getType() {
        return SOFTWARE_COMPOSITION_ANALYZER;
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
        if(isRescan() && isNullOrEmpty(getScanId())) {
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

        public FormValidation doCheckScanId(@QueryParameter String scanId, @RelativePath("..") @QueryParameter String application, @RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            JenkinsAuthenticationProvider provider = new JenkinsAuthenticationProvider(credentials, context);
            if(scanId!=null && !scanId.isEmpty() && !ServiceUtil.isScanId(scanId,application,SOFTWARE_COMPOSITION_ANALYZER,provider)) {
                return FormValidation.error(Messages.error_invalid_scan_id_ui());
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
