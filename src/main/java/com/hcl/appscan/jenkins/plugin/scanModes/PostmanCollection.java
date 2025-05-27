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
        properties.put("scanType", POSTMAN_COLLECTION);
        if(isNullOrEmpty(m_postmanCollectionFile)) {
            properties.put("postmanCollectionFile", resolver == null ? m_postmanCollectionFile : resolvePath(m_postmanCollectionFile, resolver));
        }
        if (isNullOrEmpty(m_additionalDomains)) {
            properties.put("additionalDomains", resolver == null ? m_additionalDomains : Util.replaceMacro(m_additionalDomains, resolver));
        }
        if(isNullOrEmpty(m_environmentalVariablesFile)) {
            properties.put("environmentalVariablesFile", resolver == null ? m_environmentalVariablesFile : resolvePath(m_environmentalVariablesFile, resolver));
        }
        if(isNullOrEmpty(m_globalVariablesFile)) {
            properties.put("globalVariablesFile", resolver == null ? m_globalVariablesFile : resolvePath(m_globalVariablesFile, resolver));
        }
        if(isNullOrEmpty(m_additionalFiles)) {
            properties.put("additionalFiles", resolver == null ? m_additionalFiles : resolvePath(m_additionalFiles, resolver));
        }
        return properties;
    }

    @Symbol("postman_collection") //$NON-NLS-1$
    @Extension
    public static final class DescriptorImpl extends ScanModeDescriptor {

        @Override
        public String getDisplayName() {
            return "Postman Collection";
        }

        public FormValidation doCheckPostmanCollectionFile(@QueryParameter String postmanCollectionFile) {
            if(postmanCollectionFile != null && !postmanCollectionFile.isEmpty() && !postmanCollectionFile.endsWith(".json")) {
                return FormValidation.error(Messages.error_file_type_invalid_json());
            }
            return FormValidation.validateRequired(postmanCollectionFile);
        }

        public FormValidation doCheckAdditionalDomains(@QueryParameter String additionalDomains) {
            return FormValidation.validateRequired(additionalDomains);
        }

        public FormValidation doCheckEnvironmentalVariablesFile(@QueryParameter String environmentalVariablesFile) {
            if(environmentalVariablesFile != null && !environmentalVariablesFile.isEmpty() && !environmentalVariablesFile.endsWith(".json")) {
                return FormValidation.error(Messages.error_file_type_invalid_json());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckGlobalVariablesFile(@QueryParameter String globalVariablesFile) {
            if(globalVariablesFile != null && !globalVariablesFile.isEmpty() && !globalVariablesFile.endsWith(".json")) {
                return FormValidation.error(Messages.error_file_type_invalid_json());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckAdditionalFiles(@QueryParameter String additionalFiles) {
            if(additionalFiles != null && !additionalFiles.isEmpty() && !additionalFiles.endsWith(".zip")) {
                return FormValidation.error(Messages.error_file_type_invalid_zip());
            }
            return FormValidation.ok();
        }
    }
}
