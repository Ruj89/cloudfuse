package net.ruj.cloudfuse.clouds.gdrive;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.fuse.CloudFileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GDriveController {
    private final CloudFileSystemService cloudFileSystemService;
    private final OAuth2RestTemplate oAuth2RestTemplate;

    @Autowired
    public GDriveController(
            CloudFileSystemService cloudFileSystemService,
            OAuth2RestTemplate oAuth2RestTemplate
    ) {
        this.cloudFileSystemService = cloudFileSystemService;
        this.oAuth2RestTemplate = oAuth2RestTemplate;
    }

    @GetMapping("/mount")
    public void mount() throws IllegalAccessException {
        CloudStorageService cloudStorageService = new GDriveService(oAuth2RestTemplate.getAccessToken().getValue());
        cloudFileSystemService.init(cloudStorageService);
    }
}
