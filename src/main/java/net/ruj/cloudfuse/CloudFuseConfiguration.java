package net.ruj.cloudfuse;

import net.ruj.cloudfuse.clouds.gdrive.GDriveConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloudfuse.fuse.defaults")
public class CloudFuseConfiguration {
    private GDriveConfiguration googledrive;
    private boolean automount;

    public GDriveConfiguration getGoogledrive() {
        return googledrive;
    }

    public void setGoogledrive(GDriveConfiguration googledrive) {
        this.googledrive = googledrive;
    }

    public boolean isAutomount() {
        return automount;
    }

    public void setAutomount(boolean automount) {
        this.automount = automount;
    }
}
