# HCL AppScan Jenkins plugin
Easily integrate security testing into your Jenkins builds using the HCL AppScan Jenkins Plug-in. This plug-in enables you to execute DAST(Dynamic Application Security Testing) scans using HCL AppScan On Cloud and HCL AppScan Enterprise.

## Note
Post upgrade to 1.3.0, existing credentials cannot be viewed or edited.
In case you need to modify the existing credentials with new ID or secret,
note down the label, delete that credential entry and create new credentials
with same label.

Older versions of this plugin may not be safe to use. Please review the
following warnings before using an older version:

-   [Plain text password shown in job configuration form
    fields](https://jenkins.io/security/advisory/2019-08-28/#SECURITY-1512)

## Prerequisites

An account at the [HCL AppScan on
Cloud](https://cloud.appscan.com/AsoCUI/serviceui/home)
service. You'll need to [create an
application](http://help.hcltechsw.com/appscan/ASoC/ent_create_application.html?query=create)
on the service to associate your scans with.

Post 1.3.0 release, HCL AppScan Jenkins Plug-in will support integration with HCL AppScan Enterprise for creation and execution of ADAC jobs. Hence, if you intend to use this integration, you would need login access to a running instance of AppScan Enterprise Server. Please note that Content Scan jobs are not supported with Jenkins integration.

## Usage
**Integration with HCL AppScan On Cloud**

[This video](http://ibm.biz/ASoC-Jenkins) demonstrates
installation and configuration of the plugin for HCL AppScan On Cloud.

1.  Add your AppScan on Cloud credentials on the Jenkins
    **Credentials** page.
    -   From the main Jenkins dashboard, click the **Credentials** link.
    -   Add new global credentials.
    -   In the **Kind** drop-down list, select **HCL AppScan on Cloud Credentials**.
    -   Enter your API key details.
2.  Add a **Run AppScan On Cloud Security Test** build step to your Jenkins project
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
        -   **Mobile Analyzer**
            -   **Application File**: Enter the full path and file name
                of the .apk or .ipa file that you want to scan.
            -   **Additional Options**: If selected, the following
                options are available.
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
        -   **Static Analyzer**
            -   **Target**: Enter the full path to the directory that
                contains the files that you want to scan or enter the
                full path to an existing .irx file.
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
            -   **Template**: Select the template for the scan.
            -   **Job name**: Specify a name to use for the scan. This value
                will be used to distinguish this scan and its results from
                others.
            -   **Scan folder**: Select the destination folder to create the ADAC job. 
            -   **Application name**: Select the application to associate the scan with. 
                Please note this is not a required parameter for an AppScan Enterprise 
                ADAC job.
            -   **Test policy**: Select the test policy for your scan.
            -   **Starting URL**: Enter the URL from where you want the
                scan to start exploring the site.                
       - **Which Login method you want to use**       
            - **Recorded login**: Select this option to allow login to the application
              using a recorded login sequence. Once selected, you would be prompted
              to enter the path to recorded login sequence.
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

<http://help.hcltechsw.com/appscan/ASoC/appseccloud_jenkins.html?query=jenkins>

## Release History

### 1.3.0 (January, 2020)

-  HCL Washed Release with support for HCL AppScan Enterprise ADAC Jobs.

### 1.2.6 (November, 2019)

-   Bug fixes.

### 1.2.5 (August, 2019)

-   Bug fixes.

### 1.2.4 (May, 2019)

-   Update service url to cloud.appscan.com

### 1.2.3 (April, 2019)

-   Support for "Normal" or "Optimized" DAST scans.

### 1.2.2 (October, 2018)

-   Fail builds for noncompliance with application policies.
-   Noncompliant issues reports.
-   Open source only scans.
-   Bug fixes.

### 1.2.1 (July, 2018)

-   Update for
    [JEP-200](https://jenkins.io/blog/2018/01/13/jep-200/).

### 1.2.0 (July, 2018)

-   Pipeline support
-   Sort the application list alphabetically

### 1.1.3 (July, 2018)

-   Allow ".scan" files in addition to ".scant" for dynamic scans.

### 1.1.2 (September, 2017)

-   Bug fixes.

### 1.1.1 (May, 2017)

-   Drop-down list for selecting a Presence for Mobile and Dynamic
    scans.
-   Enter an existing .irx file in the Target field of a Static scan.

### 1.1 (February, 2017)

-   Support for dynamic scanning.
-   Additional options for mobile and dynamic scans.
-   Authentication changed from username and password to API key.

### 1.0 (December, 2016)

-   Initial release.

