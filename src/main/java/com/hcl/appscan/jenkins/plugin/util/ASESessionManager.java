package com.hcl.appscan.jenkins.plugin.util;

import com.hcl.appscan.jenkins.plugin.auth.ASECredentials;
import com.hcl.appscan.jenkins.plugin.auth.ASECredentialsWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * This Class manages ASE Credentials Object so that Credentials with similar Configuration is reused to restrict
 * creation of ASE Sessions
 * */
public class ASESessionManager {
    private static Map<ASECredentialsWrapper, ASECredentials> m_credentialMap = new HashMap<>();
    private static final Object m_object = new Object();

    public static ASECredentials getASECredentialObject(ASECredentials credentials) {
        ASECredentials aseCredentials = null;
        ASECredentialsWrapper credentialsWrapper = new ASECredentialsWrapper(credentials);
        synchronized (m_object) {
            // If Credential with similar Configuration exists that will be reused or new entry will be created
            if (m_credentialMap.containsKey(credentialsWrapper)) {
                aseCredentials = m_credentialMap.get(credentialsWrapper);
            } else {
                m_credentialMap.put(credentialsWrapper, credentials);
                aseCredentials = credentials;
            }
        }

        return aseCredentials;
    }

    public static String toStringMap() {
        return "Size = " + m_credentialMap.size() + ", " + m_credentialMap.toString();
    }
}
