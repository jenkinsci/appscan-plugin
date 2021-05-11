/**
 * © Copyright HCL Technologies Ltd. 2017, 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.util;

import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.util.VariableResolver;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class BuildVariableResolver implements VariableResolver<String>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Map<String, String> m_variables;
	
	public BuildVariableResolver(AbstractBuild<?,?> build, TaskListener listener) {
		m_variables = build.getBuildVariables();
		try {
			m_variables.putAll(build.getEnvironment(listener));
		} catch (IOException | InterruptedException e) {
		}
	}
	
	@Override
	public String resolve(String arg0) {
		String resolved = m_variables.get(arg0);
		
		if (resolved == null) {
			return arg0;
		}
		
		return resolved;
	}
}
