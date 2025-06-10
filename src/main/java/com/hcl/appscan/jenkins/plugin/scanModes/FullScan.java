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
    private String m_accessId;
    private Secret m_secretKey;
    private String m_exploreData;

    @DataBoundConstructor
    public FullScan(String loginType, String accessId, String secretKey, String trafficFile, String exploreData) {
        m_loginType = loginType;
        m_accessId = accessId;
        m_secretKey = Secret.fromString(secretKey);
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
    public void setAccessId(String accessId) {
            m_accessId = accessId;
    }

    public String getAccessId() {
        return m_accessId;
    }

    @DataBoundSetter
    public void setSecretKey(String secretKey) {
            m_secretKey = Secret.fromString(secretKey);
    }

    public String getSecretKey() {
        return Secret.toString(m_secretKey);
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
        properties.put(ScanModeConstants.EXPLORE_DATA, resolver == null || EMPTY.equals(m_exploreData) ? m_exploreData : resolvePath(m_exploreData, resolver));
        if (ScanModeConstants.MANUAL.equals(m_loginType)) {
            properties.put(ScanModeConstants.TRAFFIC_FILE, resolver == null || EMPTY.equals(m_trafficFile) ? m_trafficFile : resolvePath(m_trafficFile, resolver));
        } else if(ScanModeConstants.AUTOMATIC.equals(m_loginType)) {
            properties.put(ScanModeConstants.USER_NAME, resolver == null ? m_accessId : Util.replaceMacro(m_accessId, resolver));
            properties.put(ScanModeConstants.PASSWORD, resolver == null ? Secret.toString(m_secretKey) : Util.replaceMacro(Secret.toString(m_secretKey), resolver));
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
