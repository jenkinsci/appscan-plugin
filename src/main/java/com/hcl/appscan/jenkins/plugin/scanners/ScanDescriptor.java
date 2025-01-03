package com.hcl.appscan.jenkins.plugin.scanners;

import com.hcl.appscan.jenkins.plugin.Messages;
import com.hcl.appscan.sdk.CoreConstants;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

public abstract class ScanDescriptor extends Descriptor<Scanner> {
	public ScanDescriptor() {
	}

    protected FormValidation scanIdValidation(JSONObject scanDetails, String application) throws JSONException {
            if(scanDetails == null) {
                return FormValidation.error(Messages.error_invalid_scan_id_ui());
            } else {
                String status = scanDetails.getJSONObject("LatestExecution").getString("Status");
                if (!(status.equals("Ready") || status.equals("Paused") || status.equals("Failed"))) {
                    return FormValidation.error(Messages.error_scan_id_validation_status(status));
                } else if (!scanDetails.get("RescanAllowed").equals(true) && scanDetails.get("ParsedFromUploadedFile").equals(true)) {
                    return FormValidation.error(Messages.error_invalid_scan_id_rescan_allowed_ui());
                } else if (!scanDetails.get(CoreConstants.APP_ID).equals(application)) {
                    return FormValidation.error(Messages.error_invalid_scan_id_application_ui());
                }
            }
            return FormValidation.ok();
    }
}
