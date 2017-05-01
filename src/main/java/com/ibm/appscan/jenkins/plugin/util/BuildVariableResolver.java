/**
 * © Copyright HCL Technologies Ltd. 2017.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.util;

import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.util.VariableResolver;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

public class BuildVariableResolver implements VariableResolver<String>, Serializable {

	private static final long serialVersionUID = 1L;
	private static final String VAR_START = "${";
	private static final String VAR_END = "}";
	
	private Map<String, String> m_variables;
	
	public BuildVariableResolver(AbstractBuild<?,?> build, BuildListener listener) {
		m_variables = build.getBuildVariables();
		try {
			m_variables.putAll(build.getEnvironment(listener));
		} catch (IOException | InterruptedException e) {
		}
	}
	
	@Override
	public String resolve(String arg0) {
		StringBuilder builder = new StringBuilder();
		
		for(String part : arg0.split(Pattern.quote(File.separator))) {
			if(part.startsWith(VAR_START) && part.endsWith(VAR_END))
				builder.append(resolveVariable(part) + File.separator);
			else
				builder.append(part + File.separator);
		}
		//Remove trailing separator
		return builder.substring(0, builder.length() - 1);
	}

	private String resolveVariable(String var) {
		String resolved = var.substring(2, var.length() - 1);
		return m_variables.get(resolved) == null ? var : m_variables.get(resolved);
	}
}
