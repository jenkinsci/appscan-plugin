/**
 * @ Copyright IBM Corporation 2016.
 * @ Copyright HCL Technologies Ltd. 2017, 2024, 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.jenkins.plugin.scanners;

import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.jenkins.plugin.auth.JenkinsAuthenticationProvider;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.logging.Message;
import com.hcl.appscan.sdk.scan.CloudScanServiceProvider;
import hudson.AbortException;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.util.VariableResolver;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class Scanner extends AbstractDescribableImpl<Scanner> implements ScannerConstants, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String m_target;
	private boolean m_hasOptions;
	private boolean m_rescan;
	private String m_scanId;
	
	public Scanner(String target, boolean hasOptions) {
		m_target = target;
		m_hasOptions = hasOptions;
	}

	public Scanner(String target, boolean hasOptions, boolean rescan, String scanId) {
		m_target = target;
		m_hasOptions = hasOptions;
		m_rescan = rescan;
		m_scanId = scanId;
	}
	
	public boolean getHasOptions() {
		return m_hasOptions;
	}
	
	public String getTarget() {
		return m_target;
	}

	@DataBoundSetter
	public void setRescan(boolean rescan) {
		m_rescan = rescan;
	}

	public boolean getRescan() {
		return m_rescan;
	}

	@DataBoundSetter
	public void setScanId(String scanId) {
		m_scanId = scanId;
	}

	public String getScanId() {
		return m_scanId;
	}
	
	public abstract Map<String, String> getProperties(VariableResolver<String> resolver) throws AbortException;

	public abstract String getType();

	public boolean isNullOrEmpty(String string) { return string != null && !string.trim().isEmpty(); }
	
	protected String resolvePath(String path, VariableResolver<String> resolver) {
		//First replace any variables in the path
		path = Util.replaceMacro(path, resolver);
		Pattern pattern = Pattern.compile("^(\\\\|/|[a-zA-Z]:\\\\)");

		//If the path is not absolute, make it relative to the workspace
		if(!pattern.matcher(path).find()){
			String targetPath = "${WORKSPACE}" + "/" + path ;
			targetPath = Util.replaceMacro(targetPath, resolver);
			return targetPath;
		}

		return path;
	}
    public void validateSettings(JenkinsAuthenticationProvider authProvider, Map<String, String> properties, IProgress progress, boolean isAppScan360) throws IOException {
        if(authProvider.isTokenExpired()) {
            throw new AbortException(Messages.error_token_authentication());
        }

        if (isAppScan360) {
            if (!properties.get("FullyAutomatic").equals("true")) {
                progress.setStatus(new Message(Message.WARNING, Messages.warning_allow_intervention_AppScan360()));
            }
        } else if (authProvider.getacceptInvalidCerts()) {
            progress.setStatus(new Message(Message.WARNING, Messages.warning_asoc_certificates()));
        }

        if(properties.containsKey(CoreConstants.SCAN_ID)) {
            if (properties.get(CoreConstants.PERSONAL_SCAN).equals("true")) {
                progress.setStatus(new Message(Message.WARNING, Messages.warning_personal_scan_rescan()));
            }
            try {
                validateScanID(properties, authProvider);
            } catch (JSONException e) {
                //Ignore and move on.
            }
        }
    }

    private void validateScanID(Map<String, String> properties, JenkinsAuthenticationProvider authProvider) throws JSONException, IOException {
        JSONObject scanDetails = new CloudScanServiceProvider(authProvider).getScanDetails(properties.get(CoreConstants.SCANNER_TYPE), properties.get(CoreConstants.SCAN_ID));;
        if(scanDetails == null) {
            throw new AbortException(Messages.error_invalid_scan_id());
        } else {
            String status = scanDetails.getJSONObject("LatestExecution").getString("Status");
            if(!(status.equals("Ready") || status.equals("Paused") || status.equals("Failed"))) {
                throw new AbortException(Messages.error_scan_id_validation_status(status));
            } else if (!scanDetails.get("RescanAllowed").equals(true) && scanDetails.get("ParsedFromUploadedFile").equals(true)) {
                throw new AbortException(Messages.error_scan_id_validation_rescan_allowed());
            } else if (!scanDetails.get(CoreConstants.APP_ID).equals(properties.get(CoreConstants.APP_ID))) {
                throw new AbortException(Messages.error_invalid_scan_id_application());
            }
        }
    }
}
