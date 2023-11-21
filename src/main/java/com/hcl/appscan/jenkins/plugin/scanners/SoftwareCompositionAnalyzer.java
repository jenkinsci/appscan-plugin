/**
 * @ Copyright HCL Technologies Ltd. 2023.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
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

    public SoftwareCompositionAnalyzer(String target){
        super(target, false);
    }

    @DataBoundConstructor
    public SoftwareCompositionAnalyzer(String target, boolean hasOptions){
        super(target, false);
    }


    @Override
    public String getType() {
        return SOFTWARE_COMPOSITION_ANALYZER;
    }

    public Map<String, String> getProperties(VariableResolver<String> resolver) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(TARGET, resolver == null ? getTarget() : resolvePath(getTarget(), resolver));
        return properties;
    }

    @Symbol("software_composition_analysis") //$NON-NLS-1$
    @Extension
    public static final class DescriptorImpl extends ScanDescriptor {

        @Override
        public String getDisplayName() {
            return "Software Composition Analysis (SCA)";
        }

        public FormValidation doCheckTarget(@QueryParameter String target, @RelativePath("..") @QueryParameter String credentials, @AncestorInPath ItemGroup<?> context) {
            JenkinsAuthenticationProvider authProvider = new JenkinsAuthenticationProvider(credentials,context);
            if(authProvider.isAppScan360()){
                return FormValidation.error(Messages.error_sca_AppScan360_ui());
            }
            return FormValidation.ok();
        }
    }
}
