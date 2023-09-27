/**
 * @ Copyright HCL Technologies Ltd. 2023.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import hudson.Extension;
import hudson.util.VariableResolver;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.Map;

public class SoftwareCompositionAnalysis extends Scanner {
    private static final String SOFTWARE_COMPOSITION_ANALYSIS = "Sca"; //$NON-NLS-1$

    public SoftwareCompositionAnalysis(String target){
        super(target, false);
    }

    @DataBoundConstructor
    public SoftwareCompositionAnalysis(String target, boolean hasOptions){
        super(target, false);
    }


    @Override
    public String getType() {
        return SOFTWARE_COMPOSITION_ANALYSIS;
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
    }
}
