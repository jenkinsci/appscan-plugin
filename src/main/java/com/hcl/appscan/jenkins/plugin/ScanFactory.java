/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import jenkins.model.Jenkins;

import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.scan.IScan;
import com.hcl.appscan.sdk.scan.IScanFactory;

public final class ScanFactory {

	private static final ServiceLoader<IScanFactory> LOADER = createServiceLoader();
	
	public static List<String> getScanTypes() {
		ArrayList<String> types = new ArrayList<String>();
		for(IScanFactory factory : LOADER)
			types.add(factory.getType());		
		return types;
	}
	
	public static IScan createScan(Map<String, String> properties, IProgress progress, IAuthenticationProvider authProvider) {
		for(IScanFactory factory : LOADER) {
			if(factory.getType().equalsIgnoreCase(properties.get(CoreConstants.SCANNER_TYPE))) {
				return factory.create(properties, progress, authProvider);
			}
		}
		return null;
	}
	
	private static ServiceLoader<IScanFactory> createServiceLoader() {
		Jenkins jenkins = Jenkins.getInstance();
		return jenkins == null ? ServiceLoader.load(IScanFactory.class) : ServiceLoader.load(IScanFactory.class, jenkins.getPluginManager().uberClassLoader); 
	}
}
