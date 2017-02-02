package com.ibm.appscan.jenkins.plugin.scanners;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;

public class StaticAnalyzer extends Scanner {

	private static final String STATIC_ANALYZER = "Static Analyzer"; //$NON-NLS-1$
	
	@DataBoundConstructor
	public StaticAnalyzer(String target) {
		super(target, false);
	}

	@Override
	public String getType() {
		return STATIC_ANALYZER;
	}
	
	@Extension
	public static final class DescriptorImpl extends ScanDescriptor {
		
		@Override
		public String getDisplayName() {
			return STATIC_ANALYZER;
		}
	}
}
