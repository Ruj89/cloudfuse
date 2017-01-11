package net.ruj.cloudfuse.fuse;

import net.ruj.cloudfuse.clouds.gdrive.DriveConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloudfuse.fuse.defaults")
public class FuseConfiguration {
    private DriveConfiguration drive;

    public DriveConfiguration getDrive() {
        return drive;
    }

    public void setDrive(DriveConfiguration drive) {
        this.drive = drive;
    }
}
