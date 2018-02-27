package net.ruj.cloudfuse.clouds.gdrive;

import net.ruj.cloudfuse.clouds.CloudStorageConnectorAbstractController;
import net.ruj.cloudfuse.clouds.exceptions.MakeRootException;
import net.ruj.cloudfuse.database.services.TokenService;
import net.ruj.cloudfuse.fuse.CloudFileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/gdrive")
public class GDriveConnectorController extends CloudStorageConnectorAbstractController {
    @Autowired
    public GDriveConnectorController(
            CloudFileSystemService cloudFileSystemService,
            OAuth2RestTemplate oAuth2RestTemplate,
            TokenService tokenService) throws MakeRootException, IllegalAccessException {
        super(cloudFileSystemService, oAuth2RestTemplate, tokenService);
        cloudFileSystemService.addCloudStorageService(new GDriveService(tokenService));
    }

    @Override
    @GetMapping("/connect")
    public void connect() throws IllegalAccessException, MakeRootException {
        super.connect();
    }
}