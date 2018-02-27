package net.ruj.cloudfuse.clouds;

import net.ruj.cloudfuse.clouds.exceptions.MakeRootException;
import net.ruj.cloudfuse.database.models.Token;
import net.ruj.cloudfuse.database.services.TokenService;
import net.ruj.cloudfuse.fuse.CloudFileSystemService;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;

public abstract class CloudStorageConnectorAbstractController {
    private final CloudFileSystemService cloudFileSystemService;
    private final OAuth2RestTemplate oAuth2RestTemplate;
    private final TokenService tokenService;

    public CloudStorageConnectorAbstractController(
            CloudFileSystemService cloudFileSystemService,
            OAuth2RestTemplate oAuth2RestTemplate,
            TokenService tokenService) {
        this.cloudFileSystemService = cloudFileSystemService;
        this.oAuth2RestTemplate = oAuth2RestTemplate;
        this.tokenService = tokenService;
    }

    public void connect() throws IllegalAccessException, MakeRootException {
        Token token = new Token();
        token.setToken(oAuth2RestTemplate.getAccessToken().getValue());
        token.setRefreshToken(oAuth2RestTemplate.getAccessToken().getRefreshToken().getValue());
        token.setExpirationDate(oAuth2RestTemplate.getAccessToken().getExpiration());
        tokenService.update(token);
    }
}
