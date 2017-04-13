/**
 * © Copyright HCL Technologies Ltd. 2017.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin.util;

import hudson.model.BuildListener;

import java.io.Serializable;

import com.ibm.appscan.plugin.core.logging.DefaultProgress;

public class ScanProgress extends DefaultProgress implements Serializable {

	private static final long serialVersionUID = 1L;

	public ScanProgress(BuildListener listener) {
		super(listener.getLogger());
	}
}
