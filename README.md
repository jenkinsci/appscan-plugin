# HCL AppScan Jenkins plugin
Easily integrate security testing into your Jenkins builds using the HCL AppScan Jenkins Plug-in. This plug-in enables you to execute SAST (Static Application Security Testing) scan using HCL AppScan on Cloud and DAST (Dynamic Application Security Testing) scans using both HCL AppScan on Cloud and HCL AppScan Enterprise.

## Prerequisites

The plugin supports scanning through HCL AppScan on Cloud and HCL AppScan Enterprise.

**HCL AppScan on Cloud prerequisites**

An account at the [HCL AppScan on
Cloud](https://cloud.appscan.com/AsoCUI/serviceui/home)
service. You'll need to [create an
application](http://help.hcltechsw.com/appscan/ASoC/ent_create_application.html?query=create)
on the service to associate your scans with.

**HCL AppScan Enterprise prerequisites**

HCL AppScan Jenkins Plug-in supports integration with HCL AppScan Enterprise for creation and execution of ADAC jobs. To use this integration, you must have access to a running instance of AppScan Enterprise Server  version 9.0.3.14 or later. Please note that Content Scan jobs are not supported through this integration.

## Usage
**Integration with HCL AppScan on Cloud**

[This video](http://ibm.biz/ASoC-Jenkins) demonstrates
installation and configuration of the plugin for HCL AppScan on Cloud.

1.  Add your AppScan on Cloud credentials on the Jenkins
    **Credentials** page.
    -   From the main Jenkins dashboard, click the **Credentials** link.
    -   Add new global credentials.
    -   In the **Kind** drop-down list, select **HCL AppScan on Cloud Credentials**.
    -   Enter your API key details.
2.  Add a **Run AppScan on Cloud Security Test** build step to your Jenkins project
    configuration and enter the following information:
    -   **Credentials:** Select the credentials you added to Jenkins in
        step 1 above.
    -   **Application:** Select the application to associate the scan
        with. NOTE: You must create at least 1 application in the
 	HCL AppScan on Cloud](https://cloud.appscan.com) service or
        this field will be empty.
    -   **Test Name:** Specify a name to use for the scan. This value
        will be used to distinguish this scan and its results from
        others.
    -   **Test Type:** Select the type of scan to run from the available
        options.
        -   **Dynamic Analyzer**
            -   **Starting URL**: Enter the URL from where you want the
                scan to start exploring the site.
            -   **Additional Options**: If selected, the following
                options are available.
                -   **Scan Type**: Select whether your site is a Staging
                    site (under development) or a Production site (live
                    and in use).
                -   **Test Optimization**: Following options are available:
                    - **Fast**: Select this option for approximately 97% issue coverage and twice as fast test stage speed.
                                  Recommended for security experts for more frequent scans.
                    - **Faster**: Select this option for approximately 85% issue coverage and five times as fast test stage speed.
                                  Recommended for DevSecOps during ongoing evaluation.
                    - **Fastest**: Select this option for approximately 70% issue coverage and ten times as fast test stage speed.
                                  Recommended for Dev and QA during initial evaluation.
                    - **No Optimization**: Select this option for maximum issue coverage and longest scan. Recommended for security
                      experts before major releases, compliance testing and benchmarks.								
                -   **Login User** and **Login Password**: If your app
                    requires login, enter valid user credentials so that
                    Application Security on Cloud can log in to the
                    site.
                -   **Extra Field**: If your app requires a third
                    credential, enter it in this field.
                -   **Presence**: If your app is not on the internet,
                    select your AppScan Presence from the list.
                    Information about creating an AppScan Presence is
                    available
                    [here](https://help.hcltechsw.com/appscan/ASoC/asp_scanning.html).
                -   **Scan File**: If you have an AppScan Standard scan
                    file, enter its full path and file name in this
                    field. To learn more about AppScan Standard scan
                    files, see [this
                    topic](https://help.hcltechsw.com/appscan/ASoC/asd_AppScanStandard.html).
        -   **Static Analyzer**
            -   **Target**: Enter the full path to the directory that
                contains the files that you want to scan or enter the
                full path to an existing .irx file.
    -   **Allow intervention by scan enablement team:** When selected (default),
        our scan enablement team will step in if the scan fails, or if 
        no issues are found, and try to fix the configuration. This may delay 
        the scan result.            
    -   **Suspend job until security analysis completes:** If selected,
        the Jenkins job will pause until security analysis has completed
        and the results have been retrieved from the service. If
        unselected, the job will continue once the scan has been
        submitted to the analysis service.
    -   **Fail job if:** If selected, the Jenkins job will fail if the
        finding count(s) exceed the specified thresholds (see below).
    -   **Add Condition:** Allows you to add thresholds for the number
        of findings that will cause a build to fail. You can specify
        thresholds for total, high, medium, and/or low finding counts.
        If multiple conditions are added, they will be treated as though
        they are separated by a logical OR.

**Integration with HCL AppScan Enterprise**

1.  Add your AppScan Enterprise credentials on the Jenkins
    **Credentials** page.
    -   From the main Jenkins dashboard, click the **Credentials** link.
    -   Add new global credentials.
    -   In the **Kind** drop-down list, select **HCL AppScan Enterprise Credentials**.
    -   Enter your AppScan Enterprise server URL. For e.g. https:// ASE Server hostname:9443/ase
    -   Enter your API key details.
    
2.  Add a **Run AppScan Enterprise Security Test** build step to your Jenkins project
    configuration and enter the following information:    
    -   **Credentials**: Select the credentials you added to Jenkins in
        step 1 above.        
       - **Job properties**        
            -   **Template**: Search the template for the scan.
            -   **Job name**: Specify a name to use for the scan. This value
                will be used to distinguish this scan and its results from
                others.
            -   **Scan folder**: Search for the destination folder to create the ADAC job. 
            -   **Application name**: Search for the application with which the scan will be associated.
                Please note this is not a required parameter for an AppScan Enterprise 
                ADAC job.
            -   **Test policy**: Select the test policy for your scan.
            -   **Starting URL**: Enter the URL from where you want the
                scan to start exploring the site.                
       - **Which Login method you want to use**       
            - **Recorded login**: Select this option to allow login to the application
              using a recorded login sequence. Once selected, you would be prompted
              to enter the path to recorded login sequence. Supported file formats are EXD, HAR, DAST.CONFIG and LOGIN.
              Please note that .login file is supported from AppScan Enterprise 10.0.4 release onwards.
            - **Automatic login**: Select this option to allow Login to the application
              using a username and password. Once selected, you would be prompted
              to enter the username and password.
            - **Login not required**: Selecting this option will result in AppScan not
              scanning pages that require a login.            
       - **Scan**       
            - **Full automatic scan**: Select this option to automatically explore
              and test your web application              
            - **Test only (manual explore data is required)**: Select this option to run the test
              only on external recorded manual explore data.            
       - **Manual Explore (externally recorded)**       
            - **Path**: Enter the local path to the Manual explore data, to use as part of a new scan.
              For example: "C:\samplefileName.dast.config".
              Supported Manual explore file formats are: EXD, HAR, DAST.CONFIG, and CONFIG.
       - **Test Optimization**
            - **Fast**: Select this option for approximately 97% issue coverage and twice as fast test stage speed.
			  Recommended for security experts for more frequent scans.
            - **Faster**: Select this option for approximately 85% issue coverage and five times as fast test stage speed.
			  Recommended for DevSecOps during ongoing evaluation.
            - **Fastest**: Select this option for approximately 70% issue coverage and ten times as fast test stage speed.
			  Recommended for Dev and QA during initial evaluation.
            - **No Optimization**: Select this option for maximum issue coverage and longest scan. Recommended for security
              experts before major releases, compliance testing and benchmarks.            
            **Note**: The options of “Faster” and “Fastest” are supported only for AppScan Enterprise version 10.0.0 and above.		
       - **More Options**: If selected, the following option is available.
            - **Designated Agent Server**: Select the agent server to execute the job.
              Default will be considered in absence of any selection.
      -  **Do not execute the next steps in the Jenkins job sequence, until AppScan security analysis completes.:** If selected,
        the Jenkins job will pause until security analysis has completed
        and the results have been retrieved. If unselected, the job will
        continue once the scan has been submitted to the analysis service.
      - **Fail the build if one of the following conditions apply:** If selected, the Jenkins job will fail if the
        finding count(s) exceed the specified thresholds (see below).
        -   **Add Condition:** Allows you to add thresholds for the number
        of findings that will cause a build to fail. You can specify
        thresholds for total, high, medium, and/or low finding counts.
        If multiple conditions are added, they will be treated as though
        they are separated by a logical OR.

## Additional Information
**Known Issues**
- ASoC is no longer providing Mobile Scan support. The Mobile Scan functionality
has been removed from Jenkins since revision 1.0.10. The existing jobs that 
have Mobile Scan configurations must therefore be updated accordingly.

<http://help.hcltechsw.com/appscan/ASoC/appseccloud_jenkins.html?query=jenkins>


