/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2024.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.hcl.appscan.sdk.logging.IProgress;
import hudson.AbortException;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.util.VariableResolver;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class Scanner extends AbstractDescribableImpl<Scanner> implements ScannerConstants, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String m_target;
	private boolean m_hasOptions;
	
	public Scanner(String target, boolean hasOptions) {
		m_target = target;
		m_hasOptions = hasOptions;
	}
	
	public boolean getHasOptions() {
		return m_hasOptions;
	}
	
	public String getTarget() {
		return m_target;
	}
	
	public abstract Map<String, String> getProperties(VariableResolver<String> resolver) throws AbortException;

	public abstract void validateSettings(JenkinsAuthenticationProvider authProvider, Map<String, String> properties, IProgress progress) throws AbortException;

	public abstract String getType();

	public boolean isNullOrEmpty(String string) { return string != null && !string.trim().isEmpty(); }
	
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
}
