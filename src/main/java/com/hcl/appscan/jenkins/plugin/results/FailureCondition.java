/**
 * © Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.results;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.Serializable;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.hcl.appscan.jenkins.plugin.Messages;

public class FailureCondition extends AbstractDescribableImpl<FailureCondition> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String m_failureType;
	private int m_threshold;
	
	@DataBoundConstructor
	public FailureCondition(String failureType, int threshold) {
		m_failureType = failureType;
		m_threshold = threshold;
	}
	
	public String getFailureType() {
		return m_failureType;
	}
	
	public int getThreshold() {
		return m_threshold;
	}
	
	@Symbol("failure_condition") //$NON-NLS-1$
	@Extension
	public static class DescriptorImpl extends Descriptor<FailureCondition> {

		@Override
		public String getDisplayName() {
			return ""; //$NON-NLS-1$
		}
		
		public ListBoxModel doFillFailureTypeItems() {
			ListBoxModel model = new ListBoxModel();
			model.add(Messages.label_total(), "total"); //$NON-NLS-1$
			model.add(Messages.label_high(), "high"); //$NON-NLS-1$
			model.add(Messages.label_medium(), "medium"); //$NON-NLS-1$
			model.add(Messages.label_low(), "low"); //$NON-NLS-1$
			return model;
		}
		
		public FormValidation doCheckThreshold(@QueryParameter String threshold) {
			return FormValidation.validateNonNegativeInteger(threshold);
		}
	}
}
