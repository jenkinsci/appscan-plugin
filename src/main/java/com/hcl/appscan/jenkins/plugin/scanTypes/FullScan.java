package com.hcl.appscan.jenkins.plugin.scanTypes;

import hudson.Extension;
import hudson.util.Secret;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Map;

public class FullScan implements IScanType {

    private String m_loginType;
    private String m_trafficFile;
    private String m_userName;
    private Secret m_password;
    private String m_exploreData;


    public FullScan() {
        this("Manual", "", "", "", "");
    }

    public FullScan(String loginType, String trafficFile, String userName, String password, String exploreData) {
        m_loginType = loginType;
        m_trafficFile = trafficFile;
        m_userName = userName;
        m_password = Secret.fromString(password);
        m_exploreData = exploreData;
    }

    @Override
    public void configureScanProperties(Map<String, String> properties) {
        properties.put("scanMode", "fullScan");
        // Add other full scan–specific properties
        properties.put("applicationId", "someAppId");
    }

    @Symbol("full_scan") //$NON-NLS-1$
    @Extension
    public static final class DescriptorImpl extends ScanTypeDescriptor {

        @Override
        public String getDisplayName() {
            return "Full Scan";
        }
    }
}
