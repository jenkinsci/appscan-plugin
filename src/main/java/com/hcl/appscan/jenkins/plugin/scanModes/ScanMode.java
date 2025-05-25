package com.hcl.appscan.jenkins.plugin.scanModes;

import com.hcl.appscan.jenkins.plugin.scanners.ScannerConstants;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.util.VariableResolver;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class ScanMode extends AbstractDescribableImpl<ScanMode> implements ScannerConstants, Serializable {



    public ScanMode() {
    }

    /**
     * Configure scan properties based on the provided properties and variable resolver.
     *
     * @param properties The initial properties to configure.
     * @param resolver   The variable resolver to use for resolving variables in the properties.
     * @return A map of configured scan properties.
     */
    public abstract Map<String, String> configureScanProperties(Map<String, String> properties, VariableResolver<String> resolver);

    protected String resolvePath(String path, VariableResolver<String> resolver) {
        //First replace any variables in the path
        path = Util.replaceMacro(path, resolver);
        Pattern pattern = Pattern.compile("^(\\\\|/|[a-zA-Z]:\\\\)");

        //If the path is not absolute, make it relative to the workspace
        if(!pattern.matcher(path).find()){
            String targetPath = "${WORKSPACE}" + "/" + path ;
            targetPath = Util.replaceMacro(targetPath, resolver);
            return targetPath;
        }
        return path;
    }

    protected boolean isNullOrEmpty(String string) {
        return string != null && !string.trim().isEmpty();
    }
}
