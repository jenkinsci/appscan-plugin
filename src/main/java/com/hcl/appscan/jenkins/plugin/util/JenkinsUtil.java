/**
 * @ Copyright HCL Technologies Ltd. 2022.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.util;

import com.hcl.appscan.sdk.utils.SystemUtil;
import hudson.Plugin;
import jenkins.model.Jenkins;

public class JenkinsUtil {
    
    public static String getClientType() {
        return "jenkins-" + SystemUtil.getOS() + "-" + getPluginVersion();
    }
    
    public static String getPluginVersion() {
    	Jenkins jenkins = Jenkins.getInstanceOrNull();
    	if(jenkins != null) {
	    	Plugin tempPlugin = jenkins.getPlugin("appscan");

	    	if(tempPlugin != null && tempPlugin.getWrapper() != null) {
	    		return tempPlugin.getWrapper().getVersion();
	    	}
    	}

    	return "";
    }
    
}
