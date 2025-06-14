/**
 * @ Copyright HCL Technologies Ltd. 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */


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

    private String m_loginTypeTestScan;
    private String m_trafficFileTestScan;
    private String m_accessIdTestScan;
    private Secret m_secretKeyTestScan;
    private String m_exploreDataTestScan;

    @DataBoundConstructor
    public TestOnly(String loginTypeTestScan, String accessIdTestScan, String secretKeyTestScan, String trafficFileTestScan, String exploreDataTestScan) {
        m_loginTypeTestScan = loginTypeTestScan;
        m_trafficFileTestScan = trafficFileTestScan;
        m_accessIdTestScan = accessIdTestScan;
        m_secretKeyTestScan = Secret.fromString(secretKeyTestScan);
        m_exploreDataTestScan = exploreDataTestScan;
    }

    @DataBoundSetter
    public void setLoginTypeTestScan(String loginTypeTestScan) {
        m_loginTypeTestScan = loginTypeTestScan;
    }

    public String getLoginTypeTestScan() {
        return m_loginTypeTestScan;
    }

    @DataBoundSetter
    public void setAccessIdTestScan(String accessIdTestScan) {
            m_accessIdTestScan = accessIdTestScan;
    }

    public String getAccessIdTestScan() {
        return m_accessIdTestScan;
    }

    @DataBoundSetter
    public void setSecretKeyTestScan(String secretKeyTestScan) {
            m_secretKeyTestScan = Secret.fromString(secretKeyTestScan);
    }

    public String getSecretKeyTestScan() {
        return Secret.toString(m_secretKeyTestScan);
    }

    @DataBoundSetter
    public void setTrafficFileTestScan(String trafficFileTestScan) {
            m_trafficFileTestScan = trafficFileTestScan;
    }

    public String getTrafficFileTestScan() {
        return m_trafficFileTestScan;
    }

    @DataBoundSetter
    public void setExploreDataTestScan(String exploreDataTestScan) {
            m_exploreDataTestScan = exploreDataTestScan;
    }

    public String getExploreDataTestScan() {
        return m_exploreDataTestScan;
    }

    public String isLoginTypeTestScan(String loginTypeName) {
        return loginTypeName.equalsIgnoreCase(m_loginTypeTestScan) || loginTypeName.equals(ScanModeConstants.MANUAL) ? "true" : "";
    }

    @Override
    public Map<String, String> configureScanProperties(Map<String, String> properties, VariableResolver<String> resolver) {
        properties.put(ScanModeConstants.SCAN_TYPE, TEST_ONLY);
        properties.put(ScanModeConstants.LOGIN_TYPE, m_loginTypeTestScan);
        properties.put(ScanModeConstants.EXPLORE_DATA, resolver == null || EMPTY.equals(m_exploreDataTestScan) ? m_exploreDataTestScan : resolvePath(m_exploreDataTestScan, resolver));
        if (ScanModeConstants.MANUAL.equals(m_loginTypeTestScan)) {
            properties.put(ScanModeConstants.TRAFFIC_FILE, resolver == null || EMPTY.equals(m_trafficFileTestScan) ? m_trafficFileTestScan : resolvePath(m_trafficFileTestScan, resolver));
        } else if(ScanModeConstants.AUTOMATIC.equals(m_loginTypeTestScan)) {
            properties.put(ScanModeConstants.USER_NAME, resolver == null ? m_accessIdTestScan : Util.replaceMacro(m_accessIdTestScan, resolver));
            properties.put(ScanModeConstants.PASSWORD, resolver == null ? Secret.toString(m_secretKeyTestScan) : Util.replaceMacro(Secret.toString(m_secretKeyTestScan), resolver));
        }
        return properties;
    }

    @Symbol("test_only") //$NON-NLS-1$
    @Extension
    public static final class DescriptorImpl extends ScanModeDescriptor {

        @Override
        public String getDisplayName() {
            return TEST_ONLY;
        }

        public FormValidation doCheckExploreDataTestScan(@QueryParameter String exploreDataTestScan) {
            return FormValidation.validateRequired(exploreDataTestScan);
        }
    }
}
