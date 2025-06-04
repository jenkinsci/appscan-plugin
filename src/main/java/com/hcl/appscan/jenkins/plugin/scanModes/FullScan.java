/**
 * @ Copyright HCL Technologies Ltd. 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */


package com.hcl.appscan.jenkins.plugin.scanModes;

import hudson.Extension;
import hudson.Util;
import hudson.util.Secret;
import hudson.util.VariableResolver;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.Map;

public class FullScan extends ScanMode {

    private static final String FULL_SCAN = "Full Scan"; //$NON-NLS-1$

    private String m_loginType;
    private String m_trafficFile;
    private String m_userName;
    private Secret m_password;
    private String m_exploreData;

    @DataBoundConstructor
    public FullScan(String loginType, String accessId, String secretKey, String trafficFile, String exploreData) {
        m_loginType = loginType;
        m_userName = accessId;
        m_password = Secret.fromString(secretKey);
        m_trafficFile = trafficFile;
        m_exploreData = exploreData;
    }

    @DataBoundSetter
    public void setLoginType(String loginType) {
        m_loginType = loginType;
    }

    public String getLoginType() {
        return m_loginType;
    }

    @DataBoundSetter
    public void setAccessId(String userName) {
            m_userName = userName;
    }

    public String getAccessId() {
        return m_userName;
    }

    @DataBoundSetter
    public void setSecretKey(String password) {
            m_password = Secret.fromString(password);
    }

    public String getSecretKey() {
        return Secret.toString(m_password);
    }

    @DataBoundSetter
    public void setTrafficFile(String trafficFile) {
            m_trafficFile = trafficFile;
    }

    public String getTrafficFile() {
        return m_trafficFile;
    }

    @DataBoundSetter
    public void setExploreData(String exploreData) {
        m_exploreData = exploreData;
    }

    public String getExploreData() {
        return m_exploreData;
    }

    public String isLoginType(String loginTypeName) {
        return loginTypeName.equalsIgnoreCase(m_loginType) || loginTypeName.equals(ScanModeConstants.MANUAL) ? "true" : "";
    }

    @Override
    public Map<String, String> configureScanProperties(Map<String, String> properties, VariableResolver<String> resolver) {
        properties.put(ScanModeConstants.SCAN_TYPE, FULL_SCAN);
        properties.put(ScanModeConstants.LOGIN_TYPE, m_loginType);
        properties.put("exploreData", resolver == null || m_exploreData.equals(EMPTY) ? m_exploreData : resolvePath(m_exploreData, resolver));
        if (ScanModeConstants.MANUAL.equals(m_loginType)) {
            properties.put("trafficFile", resolver == null || m_trafficFile.equals(EMPTY) ? m_trafficFile : resolvePath(m_trafficFile, resolver));
        } else if(ScanModeConstants.AUTOMATIC.equals(m_loginType)) {
            properties.put("userName", resolver == null ? m_userName : Util.replaceMacro(m_userName, resolver));
            properties.put("password", resolver == null ? Secret.toString(m_password) : Util.replaceMacro(Secret.toString(m_password), resolver));
        }
        return properties;
    }

    @Symbol("full_scan") //$NON-NLS-1$
    @Extension
    public static final class DescriptorImpl extends ScanModeDescriptor {

        @Override
        public String getDisplayName() {
            return "Full Automatic Scan";
        }
    }
}
