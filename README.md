# IBM Application Security on Cloud Jenkins Plug-in

Easily integrate security testing into your Jenkins builds using the IBM Application Security on Cloud (ASoC) Jenkins Plug-in.

# Prerequisites

- An account on the [IBM Application Security on Cloud](https://www.ibm.com/marketplace/cloud/application-security-on-cloud/) service. You'll need to [create an application](http://www.ibm.com/support/knowledgecenter/SSYJJF_1.0.0/ApplicationSecurityonCloud/ent_create_application.html) on the service to associate your scans with.

# Usage

1. Add your ASoC credentials on the Jenkins Credentials page.
  - From the main Jenkins dashboard, click the Credentials link.
  - Add new Global credentials.
  - In the "Kind" drop-down list, select "IBM Application Security on Cloud Credentials".
  - Enter your username and password.
  
2. Add a "Run Security Test" build step to your Jenkins project configuration and enter the following information:
  - <b>Credentials:</b> Select from the list of credentials you added to Jenkins in step 1 above.
  - <b>Application:</b> Select from the list of applications the selected user has access to. NOTE: You must create at least 1 application in the [IBM Application Security on Cloud](https://appscan.ibmcloud.com) service or this field will be empty.
  - <b>Test Type:</b> Select the type of scan to run from the available options.
  - <b>Target:</b> Specifies what will be scanned.
    - Static Analyzer: Specify the absolute path to a directory containing the files you want to scan.
    - Mobile Analyzer: Specify the absolute path to a .apk or .ipa file.
    - Dynamic Analyzer: Specify a url.
  - <b>Name:</b> Specify a name to use for the scan. This value will be used to distinguish this scan and it's results from others.
  - <b>Suspend job until security analysis completes (checkbox):</b> If selected, the Jenkins job will not continue until security analysis has completed and the results have been retrieved from the service. If unselected, the job will continue once the scan has been submitted to the analysis service.
  - <b>Fail job if: (checkbox):</b> If selected, the Jenkins job will fail if the finding count(s) exceed the specified thresholds (see below).
  - <b>Add Condition:</b> Allows you to add thresholds for the number of findings that will cause a build to fail. You can specify thresholds for total, high, medium, and/or low finding counts. If multiple conditions are present, they are logically OR'd together.

# License

All files found in this project are licensed under the [Apache License 2.0](LICENSE).
