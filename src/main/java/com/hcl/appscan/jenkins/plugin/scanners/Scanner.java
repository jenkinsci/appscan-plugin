/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.util.VariableResolver;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
	
	public abstract Map<String, String> getProperties(VariableResolver<String> resolver);
	
	public abstract String getType();
	
	protected String resolvePath(String path, VariableResolver<String> resolver) {
		//First replace any variables in the path
		path = Util.replaceMacro(path, resolver);
		
		//If the path is not absolute, make it relative to the workspace
		File file = new File(path);
		if(!file.isAbsolute()) {
			String targetPath = "${WORKSPACE}" + File.separator + file.getPath();
			targetPath = Util.replaceMacro(targetPath, resolver);
			file = new File(targetPath);
		}

		return file.getAbsolutePath();
	}
}
