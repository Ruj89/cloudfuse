package net.ruj.cloudfuse.clouds.gdrive;

import net.ruj.cloudfuse.clouds.CloudStorageConnectorAbstractController;
import net.ruj.cloudfuse.clouds.exceptions.MakeRootException;
import net.ruj.cloudfuse.database.services.TokenService;
import net.ruj.cloudfuse.fuse.VirtualFileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gdrive")
public class GDriveConnectorController extends CloudStorageConnectorAbstractController<GDriveService> {
    private final GDriveConfiguration configuration;

    @Autowired
    public GDriveConnectorController(
            VirtualFileSystemService virtualFileSystemService,
            OAuth2RestTemplate oAuth2RestTemplate,
            TokenService tokenService,
            GDriveConfiguration configuration) {
        super(virtualFileSystemService, oAuth2RestTemplate, tokenService);
        this.configuration = configuration;
    }

    @Override
    @GetMapping("/connect")
    public void connect() throws IllegalAccessException, MakeRootException {
        super.connect();
    }

    @Override
    protected GDriveService buildCloudStorageService() {
        return new GDriveService(tokenService, configuration);
    }
}