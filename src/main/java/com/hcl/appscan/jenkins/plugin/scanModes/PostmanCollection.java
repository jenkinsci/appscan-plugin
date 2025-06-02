/**
 * @ Copyright HCL Technologies Ltd. 2025.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */


package com.hcl.appscan.jenkins.plugin.scanModes;

import com.hcl.appscan.jenkins.plugin.Messages;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.VariableResolver;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.Map;

public class PostmanCollection extends ScanMode {

    private static final String POSTMAN_COLLECTION = "Postman Collection"; //$NON-NLS-1$

    private String m_postmanCollectionFile;
    private String m_additionalDomains;
    private String m_environmentalVariablesFile;
    private String m_globalVariablesFile;
    private String m_additionalFiles;

    @DataBoundConstructor
    public PostmanCollection() {
        this("", "", "", "", "");
    }

    public PostmanCollection(String postmanCollectionFile, String additionalDomains, String environmentalVariablesFile, String globalVariablesFile, String additionaFiles) {
        m_postmanCollectionFile = postmanCollectionFile;
        m_additionalDomains = additionalDomains;
        m_environmentalVariablesFile = environmentalVariablesFile;
        m_globalVariablesFile = globalVariablesFile;
        m_additionalFiles = additionaFiles;
    }

    @DataBoundSetter
    public void setPostmanCollectionFile(String postmanCollectionFile) {
        m_postmanCollectionFile = postmanCollectionFile;
    }

    public String getPostmanCollectionFile() {
        return m_postmanCollectionFile;
    }

    @DataBoundSetter
    public void setAdditionalDomains(String additionalDomains) {
        m_additionalDomains = additionalDomains;
    }

    public String getAdditionalDomains() {
        return m_additionalDomains;
    }

    @DataBoundSetter
    public void setEnvironmentalVariablesFile(String environmentalVariablesFile) {
        m_environmentalVariablesFile = environmentalVariablesFile;
    }

    public String getEnvironmentalVariablesFile() {
        return m_environmentalVariablesFile;
    }

    @DataBoundSetter
    public void setGlobalVariablesFile(String globalVariablesFile) {
        m_globalVariablesFile = globalVariablesFile;
    }

    public String getGlobalVariablesFile() {
        return m_globalVariablesFile;
    }

    @DataBoundSetter
    public void setAdditionalFiles(String additionalFiles) {
        m_additionalFiles = additionalFiles;
    }

    public String getAdditionalFiles() {
        return m_additionalFiles;
    }


    @Override
    public Map<String, String> configureScanProperties(Map<String, String> properties, VariableResolver<String> resolver) {
        properties.put(ScanModeConstants.SCAN_TYPE, POSTMAN_COLLECTION);
        addResolvedProperty(properties, "postmanCollectionFile", m_postmanCollectionFile, resolver);
        addResolvedProperty(properties, "additionalDomains", m_additionalDomains, resolver);
        addResolvedProperty(properties, "environmentalVariablesFile", m_environmentalVariablesFile, resolver);
        addResolvedProperty(properties, "globalVariablesFile", m_globalVariablesFile, resolver);
        addResolvedProperty(properties, "additionalFiles", m_additionalFiles, resolver);
        return properties;
    }

    private void addResolvedProperty(Map<String, String> properties, String key, String value, VariableResolver resolver) {
        if (!isNullOrEmpty(value)) return;
        String resolvedValue = (resolver == null) ? value :
                ("additionalDomains".equals(key) ? Util.replaceMacro(value, resolver) : resolvePath(value, resolver));
        properties.put(key, resolvedValue);
    }

    @Symbol("postman_collection") //$NON-NLS-1$
    @Extension
    public static final class DescriptorImpl extends ScanModeDescriptor {

        @Override
        public String getDisplayName() {
            return POSTMAN_COLLECTION;
        }

        public FormValidation doCheckAdditionalDomains(@QueryParameter String additionalDomains) {
            return FormValidation.validateRequired(additionalDomains);
        }

        // Helper method for file type validation
        private FormValidation validateFileExtension(String fileName, String extensionType, boolean required) {
            if (fileName != null && !fileName.isEmpty()) {
                if (!fileName.endsWith(extensionType)) {
                    return FormValidation.error(Messages.error_file_type_invalid(extensionType));
                }
            }
            return required ? FormValidation.validateRequired(fileName) : FormValidation.ok();
        }

        public FormValidation doCheckPostmanCollectionFile(@QueryParameter String postmanCollectionFile) {
            return validateFileExtension(postmanCollectionFile, ".json", true);
        }

        public FormValidation doCheckEnvironmentalVariablesFile(@QueryParameter String environmentalVariablesFile) {
            return validateFileExtension(environmentalVariablesFile, ".json", false);
        }

        public FormValidation doCheckGlobalVariablesFile(@QueryParameter String globalVariablesFile) {
            return validateFileExtension(globalVariablesFile, ".json", false);
        }

        public FormValidation doCheckAdditionalFiles(@QueryParameter String additionalFiles) {
            return validateFileExtension(additionalFiles, ".zip", false);
        }
    }
}
