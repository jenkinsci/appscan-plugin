/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.jenkins.plugin;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import jenkins.model.Jenkins;

import com.ibm.appscan.plugin.core.CoreConstants;
import com.ibm.appscan.plugin.core.auth.IAuthenticationProvider;
import com.ibm.appscan.plugin.core.logging.IProgress;
import com.ibm.appscan.plugin.core.scan.IScan;
import com.ibm.appscan.plugin.core.scan.IScanFactory;

public final class ScanFactory implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final ServiceLoader<IScanFactory> LOADER = createServiceLoader();
	
	private Map<String, String> m_properties;
	private IProgress m_progress;
	private IAuthenticationProvider m_authProvider;
	
	public ScanFactory(Map<String, String> properties, IProgress progress, IAuthenticationProvider authProvider) {
		m_properties = properties;
		m_progress = progress;
		m_authProvider = authProvider;
	}
	
	public static List<String> getScanTypes() {
		ArrayList<String> types = new ArrayList<String>();
		for(IScanFactory factory : LOADER)
			types.add(factory.getType());		
		return types;
	}
	
	public IScan createScan() {
		File pluginDir = new File(System.getProperty("java.io.tmpdir"), ".appscan"); //$NON-NLS-1$ //$NON-NLS-2$
    	System.setProperty(CoreConstants.SACLIENT_INSTALL_DIR, pluginDir.getAbsolutePath());
		
    	for(IScanFactory factory : LOADER) {
			if(factory.getType().equalsIgnoreCase(m_properties.get(CoreConstants.SCANNER_TYPE))) {
				return factory.create(m_properties, m_progress, m_authProvider);
			}
		}
		return null;
	}
	
	private static ServiceLoader<IScanFactory> createServiceLoader() {
		Jenkins jenkins = Jenkins.getInstance();
		return jenkins == null ? ServiceLoader.load(IScanFactory.class) : ServiceLoader.load(IScanFactory.class, jenkins.getPluginManager().uberClassLoader); 
	}
}
