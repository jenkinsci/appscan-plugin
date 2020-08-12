package com.hcl.appscan.jenkins.plugin.auth;

/**
 * This is a Wrapper Class of ASECredentials used to Override equals() and hashcode() method to be used as Key in Map,
 * as overriding these methods in ASECredentials is restricted
 * */
public class ASECredentialsWrapper {
    ASECredentials m_aseCredentials;

    public ASECredentialsWrapper(ASECredentials aseCredentials) {
        this.m_aseCredentials = aseCredentials;
    }

    public ASECredentials getAseCredentials() {
        return m_aseCredentials;
    }

    @Override
    public int hashCode() {
        return m_aseCredentials !=null ? m_aseCredentials.getId().hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ASECredentialsWrapper && m_aseCredentials != null) {
            ASECredentials credential = ((ASECredentialsWrapper) obj).getAseCredentials();
            if (credential != null) {
                return (m_aseCredentials.getId().equals(credential.getId()) &&
                        m_aseCredentials.getServer().equals(credential.getServer()) &&
                        m_aseCredentials.getUsername().equals(credential.getUsername()) &&
                        m_aseCredentials.getPassword().getPlainText().equals(credential.getPassword().getPlainText()));
            }
        }
        return false;
    }
}
