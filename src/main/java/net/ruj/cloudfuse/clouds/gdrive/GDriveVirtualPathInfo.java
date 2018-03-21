package net.ruj.cloudfuse.clouds.gdrive;

import net.ruj.cloudfuse.clouds.VirtualPathInfo;
import net.ruj.cloudfuse.clouds.gdrive.models.File;

public class GDriveVirtualPathInfo implements VirtualPathInfo {
    private File linkedFileInfo;

    public GDriveVirtualPathInfo(File linkedFileInfo) {
        this.linkedFileInfo = linkedFileInfo;
    }

    public File getLinkedFileInfo() {
        return linkedFileInfo;
    }

    public void setLinkedFileInfo(File linkedFileInfo) {
        this.linkedFileInfo = linkedFileInfo;
    }

    @Override
    public long getFileSize() {
        return linkedFileInfo.getSize();
    }
}
