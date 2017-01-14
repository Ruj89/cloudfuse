package net.ruj.cloudfuse.clouds.gdrive;

import net.ruj.cloudfuse.clouds.CloudPathInfo;
import net.ruj.cloudfuse.clouds.gdrive.models.File;

public class GDriveCloudPathInfo implements CloudPathInfo {
    private File linkedFileInfo;

    public GDriveCloudPathInfo(File linkedFileInfo) {
        this.linkedFileInfo = linkedFileInfo;
    }
}
