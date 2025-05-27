package com.hcl.appscan.jenkins.plugin.scanModes;

import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.util.VariableResolver;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.Map;

public class TestOnly extends ScanMode {

    private static final String TEST_ONLY = "Test Only"; //$NON-NLS-1$

    private String m_scanType;
    private String m_loginTypeTestScan;
    private String m_trafficFileTestScan;
    private String m_userNameTestScan;
    private Secret m_passwordTestScan;
    private String m_exploreDataTestScan;

    @DataBoundConstructor
    public TestOnly(String scanType, String loginTypeTestScan, String trafficFileTestScan, String userNameTestScan, String passwordTestScan, String exploreDataTestScan) {
        m_scanType = scanType;
        m_loginTypeTestScan = loginTypeTestScan;
        m_trafficFileTestScan = trafficFileTestScan;
        m_userNameTestScan = userNameTestScan;
        m_passwordTestScan = Secret.fromString(passwordTestScan);
        m_exploreDataTestScan = exploreDataTestScan;
    }

    @DataBoundSetter
    public void setScanType(String scanType) {
        m_scanType = scanType;
    }

    public String getScanType() {
        return m_scanType;
    }

    @DataBoundSetter
    public void setLoginTypeTestScan(String loginTypeTestScan) {
        m_loginTypeTestScan = loginTypeTestScan;
    }

    public String getLoginTypeTestScan() {
        return m_loginTypeTestScan;
    }

    @DataBoundSetter
    public void setTrafficFileTestScan(String trafficFileTestScan) {
        if("Manual".equals(m_loginTypeTestScan)) {
            m_trafficFileTestScan = trafficFileTestScan;
        }
    }

    public String getTrafficFileTestScan() {
        return m_trafficFileTestScan;
    }

    @DataBoundSetter
    public void setAccessIdTestScan(String userNameTestScan) {
        if("Automatic".equals(m_loginTypeTestScan)) {
            m_userNameTestScan = userNameTestScan;
        }
    }

    public String getAccessIdTestScan() {
        return m_userNameTestScan;
    }

    @DataBoundSetter
    public void setSecretKeyTestScan(String passwordTestScan) {
        if("Automatic".equals(m_loginTypeTestScan)) {
            m_passwordTestScan = Secret.fromString(passwordTestScan);
        }
    }

    public String getSecretKeyTestScan() {
        return Secret.toString(m_passwordTestScan);
    }

    @DataBoundSetter
    public void setExploreDataTestScan(String exploreDataTestScan) {
            m_exploreDataTestScan = exploreDataTestScan;
    }

    public String getExploreDataTestScan() {
        return m_exploreDataTestScan;
    }

    public String isLoginTypeTestScan(String loginTypeName) {
        if (m_loginTypeTestScan != null) {
            return m_loginTypeTestScan.equalsIgnoreCase(loginTypeName) ? "true" : "";
        } else if (loginTypeName.equals("Manual")) { //Default
            return "true";
        }
        return "";
    }

    @Override
    public Map<String, String> configureScanProperties(Map<String, String> properties, VariableResolver<String> resolver) {
        properties.put("scanType", TEST_ONLY);
        properties.put("loginType", m_loginTypeTestScan);
        properties.put("exploreData", resolver == null ? m_exploreDataTestScan : resolvePath(m_exploreDataTestScan, resolver));
        if ("Manual".equals(m_loginTypeTestScan)) {
            properties.put("trafficFile", resolver == null ? m_trafficFileTestScan : resolvePath(m_trafficFileTestScan, resolver));
        } else if("Automatic".equals(m_loginTypeTestScan)) {
            properties.put("accessId", resolver == null ? m_userNameTestScan : Util.replaceMacro(m_userNameTestScan, resolver));
            properties.put("secretKey", resolver == null ? Secret.toString(m_passwordTestScan) : Util.replaceMacro(Secret.toString(m_passwordTestScan), resolver));
        }
        return properties;
    }

    @Symbol("test_only") //$NON-NLS-1$
    @Extension
    public static final class DescriptorImpl extends ScanModeDescriptor {

        @Override
        public String getDisplayName() {
            return "Test Only";
        }

        public FormValidation doCheckExploreDataTestScan(@QueryParameter String exploreDataTestScan) {
            return FormValidation.validateRequired(exploreDataTestScan);
        }



    }
}
