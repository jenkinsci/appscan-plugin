/*
 * @ Copyright HCL Technologies Ltd. 2020.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
 
package com.ibm.appscan.jenkins.plugin.results;
//Added for backward compatibility during HCL wash
public class FailureCondition extends com.hcl.appscan.jenkins.plugin.results.FailureCondition {

	private static final long serialVersionUID = 1L;
	
	public FailureCondition(String failureType, int threshold) {
		super(failureType, threshold);
	}
}