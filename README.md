# HDS-Explorer Mobile
![Platform](https://img.shields.io/badge/platform-Android-blue.svg)
![Language](https://img.shields.io/badge/platform-Java-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Mobile Application for Health and Demographics Surveillance system data collection and data management that facilitates efficient, accurate data collection for program or research implementation in low-resource settings.

HDS-Explorer provides an internal forms render [HDS-Forms Library](https://github.com/philimones-group/hds-forms-lib) based on Excel file (similar to ODK) for demographics surveillance core forms data capture.  
This application also support ODK Forms data collection, allowing to effectively use HDSS dataset to implement researches with different use cases.

* HDS-Explorer website: [https://www.hds-explorer.org](https://www.hds-explorer.org)
* ODK website: [https://opendatakit.org](https://opendatakit.org)

### Features
* Fast and efficient access to Demographics Surveillance data
* ODK Forms Management System (Ability to use the Demographics Data to Collect large forms for research studies)
* Form Groups, the ability to group ODK Forms into one (Allow to split large forms into smaller and maintaining a link between them)
* Follow-up Management (Generate specific lists of Households, Members or Subjects to follow-up and collect forms)
* External Datasets, ability to visualize studies datasets on mobile app and pre-populate data into Electronics Forms (ODK)
* Project/Study based data access, grant access on specific data (Region, Household, Member, Forms) to a User that belongs to a specific Study Module




### Build/Development Instructions
HDS-Explorer Mobile is being developed using Android Framework coded in Java Programming Language.

To get started with HDS-Explorer, simply clone the repository and then from within your local copy:

**Development Instructions**  
For instructions on how to install SDKMAN visit https://sdkman.io/install
1. Download and install [Git](https://git-scm.com/downloads)

1. Download and install [Android Studio](https://developer.android.com/studio/index.html)

1. Clone HDS-Explorer mobile and HDS-Forms Library to the same folder. At the command line:

        git clone https://github.com/philimones-group/hds-explorer-tablet
        git clone https://github.com/philimones-group/hds-forms-lib

1. Also you can fork the hds-explorer-tablet project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Configure MapBox API Token on a global gradle.properties (under $USER_HOME/.gradle/gradle.properties)   
   Add the property below change the GENERATED_TOKEN for the real MapBox API Token [[Create MapBox Token]](https://docs.mapbox.com/help/tutorials/get-started-tokens-api/)  
       
        MAPBOX_HDS_EXPLORER_TOKEN=GENERATED_TOKEN

1. Use Android Studio to import the *hds-explorer-tablet* project. Then you can run/build the app.  

  
**Android APK Installation options:**
1. Download the file **hds-explorer-tablet.apk** from the repository releases https://github.com/philimones-group/hds-explorer-tablet/releases

1. Download the Android APK from the Server Application web page:  
   To be able to achieve this a copy of APK file must be in HDS-Explorer server resources directory in the Linux system
   
        sudo cp hds-explorer-tablet.apk /var/lib/hds-explorer/apks/  

* Access the link of HDS-Explorer in your local server (the link below can be found in the main page)   
  https://localhost:8443/hds-explorer-server/download/apk  
