package com.hcl.appscan.jenkins.plugin.scanTypes;

import com.hcl.appscan.jenkins.plugin.scanners.ScannerConstants;
import hudson.model.AbstractDescribableImpl;

import java.io.Serializable;

public abstract class ScanType extends AbstractDescribableImpl<ScanType> implements ScannerConstants, Serializable {



    public ScanType() {
    }
}
