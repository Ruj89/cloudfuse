**CLOUDFUSE**
=============

A Open Source Java and [FUSE](https://github.com/libfuse/libfuse) based virtual file system that mounts to a cloud storage account to a local Linux
folder.

Supported cloud storages
------------------------
* [Google Drive](https://www.google.com/drive/)

Configuration
-------------
Rename the `oauth2secrets.gradle.example` file in `oauth2secrets.gradle` and set:
* **ext.googleClientID** (_required_): the client ID of your Google Developer Console application (Google Drive API)
* **ext.googleClientSecret** (_required_): the client secret of your Google Developer Console application (Google Drive API)