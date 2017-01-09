package net.ruj.cloudfuse.fuse.configuration;

public class DriveConfiguration {
    private String localFolder = "";
    private String remoteFolder = "";

    public String getLocalFolder() {
        return localFolder;
    }

    public void setLocalFolder(String localFolder) {
        this.localFolder = localFolder;
    }

    public String getRemoteFolder() {
        return remoteFolder;
    }

    public void setRemoteFolder(String remoteFolder) {
        this.remoteFolder = remoteFolder;
    }
}
