# CLOUDFUSE

An Open Source Java 8 and FUSE based virtual file system that mounts a cloud 
storage account to a local Linux folder.

#### Supported cloud storages
* Google Drive [[1]](#link-gdrive)

## Building
Import as a new *gradle* project and execute **bootRepackage** task:
```bash
gradle bootRepackage
```
The main jar will be generated in *build\libs* folder.

## Installation
### Prerequisites
To execute the application in a local environment you need
* A Linux distribution (ex.: *CentOS 7.1* [[2]](#link-centos) )
* libFUSE [[3]](#link-libfuse)
* A Java Virtual Machine supporting Java 8 (ex.: *OpenJDK 1.8* [[4]](#link-openjdk))
* A web client supporting SSL (ex.: *Firefox 50.1* [[5]](#link-firefox))

### Cloud platforms configurations
This tutorial will be updated at CloudFuse release time and depends on user localization.

#### Google Drive
Google provide a single page guide to follow at: https://developers.google.com/drive/v3/web/enable-sdk. No additional
steps are needed.

To link a local folder to a Google Drive account enter in your **Google Developer Console** 
[[6]](#link-google-developer-console), create a new project specifying a new *Project name*.

After that in your Dashboard enable *Google Drive API*.

CloudFuse app needs full authorization to user account, so you also need to add custom App credentials. Go to 
Credentials of your project and create a new OAuth Client ID, specifying *Other* in *Application type* dropdown and a
custom name. 

After this step Google sends you your **client id** and a **client secret**.

### Configuration file
Rename the `oauth2secrets.gradle.example` file in `oauth2secrets.gradle` and set:
*   **ext.googleClientID** (*required*): the client ID of your Google Developer Console application (Google Drive API)
*   **ext.googleClientSecret** (*required*): the client secret of your Google Developer Console application 
    (Google Drive API)

## Running
Using CLI create the mounting point folder and execute the success jar:
```bash
mkdir /tmp/mnt
java -jar cloudfuse.jar
```
Using a local web client visit http://localhost:8080/mount and authorize the Google App.

## Links
1. <a name="link-gdrive"></a>https://www.google.com/drive
2. <a name="link-centos"></a>https://www.centos.org/
3. <a name="link-libfuse"></a>https://github.com/libfuse/libfuse
4. <a name="link-openjdk"></a>http://openjdk.java.net/
5. <a name="link-firefox"></a>https://www.mozilla.org/firefox
6. <a name="link-google-developer-console"></a>https://console.developers.google.com