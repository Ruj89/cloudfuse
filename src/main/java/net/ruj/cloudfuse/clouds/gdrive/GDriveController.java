package net.ruj.cloudfuse.clouds.gdrive;

import net.ruj.cloudfuse.clouds.exceptions.MakeRootException;
import net.ruj.cloudfuse.database.models.Token;
import net.ruj.cloudfuse.database.services.TokenService;
import net.ruj.cloudfuse.fuse.CloudFileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GDriveController {
    private final CloudFileSystemService cloudFileSystemService;
    private final OAuth2RestTemplate oAuth2RestTemplate;
    private final TokenService tokenService;

    @Autowired
    public GDriveController(
            CloudFileSystemService cloudFileSystemService,
            OAuth2RestTemplate oAuth2RestTemplate,
            TokenService tokenService) throws MakeRootException, IllegalAccessException {
        this.cloudFileSystemService = cloudFileSystemService;
        this.oAuth2RestTemplate = oAuth2RestTemplate;
        this.tokenService = tokenService;
        cloudFileSystemService.addCloudStorageService(new GDriveService(tokenService));
    }

    @GetMapping("/mount")
    public void mount() throws IllegalAccessException, MakeRootException {
        Token token = new Token();
        token.setToken(oAuth2RestTemplate.getAccessToken().getValue());
        token.setRefreshToken(oAuth2RestTemplate.getAccessToken().getRefreshToken().getValue());
        token.setExpirationDate(oAuth2RestTemplate.getAccessToken().getExpiration());
        tokenService.update(token);
        cloudFileSystemService.init();
    }
}
