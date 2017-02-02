package com.ibm.appscan.jenkins.plugin.scanners;

import hudson.model.AbstractDescribableImpl;

import java.util.HashMap;
import java.util.Map;

public abstract class Scanner extends AbstractDescribableImpl<Scanner> implements ScannerConstants {

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
	
	public Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(TARGET,  m_target);
		return properties;
	}
	
	public abstract String getType();
}
