package com.hcl.appscan.jenkins.plugin.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.util.VariableResolver;

public class EnvironmentVariableResolver implements VariableResolver<String>, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, String> m_variables;

	public EnvironmentVariableResolver(AbstractBuild<?,?> build, TaskListener listener) {
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
