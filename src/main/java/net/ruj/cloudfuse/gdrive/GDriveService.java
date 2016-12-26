package net.ruj.cloudfuse.gdrive;

import com.google.api.services.drive.Drive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GDriveService {
    static final Logger logger = LoggerFactory.getLogger(GDriveService.class);

    private void init() throws IOException {
        logger.info("Login to Google Drive application...");
        Drive service = Authorization.getDriveService();
    }
}
