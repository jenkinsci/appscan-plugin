/**
 * @ Copyright HCL Technologies Ltd. 2023, 2024, 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.scan.CloudScanServiceProvider;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoftwareCompositionAnalyzer extends Scanner {

    @Deprecated
    public SoftwareCompositionAnalyzer(String target){
        super(target, false);
    }

    public SoftwareCompositionAnalyzer(String target, boolean rescan, String scanId) {
        super(target, false, rescan, scanId);
    }

    @DataBoundConstructor
    public SoftwareCompositionAnalyzer(String target, boolean hasOptions){
        super(target, hasOptions, false, EMPTY);
    }


    @Override
    public String getType() {
        return SOFTWARE_COMPOSITION_ANALYZER;
    }

    @Override
    public void validateSettings(JenkinsAuthenticationProvider authProvider, Map<String, String> properties, IProgress progress, boolean isAppScan360) throws IOException {
        super.validateSettings(authProvider, properties, progress, isAppScan360);
        if(!ServiceUtil.hasScaEntitlement(authProvider)) {
            throw new AbortException(Messages.error_active_subscription_validation(getType()));
        }
    }

    public Map<String, String> getProperties(VariableResolver<String> resolver) {
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
                JSONObject scanDetails = new CloudScanServiceProvider(provider).getScanDetails(SOFTWARE_COMPOSITION_ANALYZER, scanId);
                return scanIdValidation(scanDetails, application);
            }
            return FormValidation.validateRequired(scanId);
        }

        public FormValidation doCheckTarget(@RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            JenkinsAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials,context);
            if(!ServiceUtil.hasScaEntitlement(authProvider)) {
                return FormValidation.error(Messages.error_active_subscription_validation_ui());
            }
            return FormValidation.ok();
        }
    }
}
