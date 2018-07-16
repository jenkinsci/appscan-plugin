/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.scanners;

import hudson.Extension;
import hudson.util.FormValidation;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

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
	
	@Symbol("static_analyzer") //$NON-NLS-1$
	@Extension
	public static final class DescriptorImpl extends ScanDescriptor {
		
		@Override
		public String getDisplayName() {
			return STATIC_ANALYZER;
		}
		
    	public FormValidation doCheckTarget(@QueryParameter String target) {
    		return FormValidation.validateRequired(target);
    	}
	}
}
