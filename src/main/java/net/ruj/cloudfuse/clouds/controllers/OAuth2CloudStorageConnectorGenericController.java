package net.ruj.cloudfuse.clouds.controllers;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.database.models.Token;
import net.ruj.cloudfuse.database.services.TokenService;
import net.ruj.cloudfuse.fuse.VirtualFileSystemService;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;

public abstract class OAuth2CloudStorageConnectorGenericController<T extends CloudStorageService> extends
        CloudStorageConnectorGenericController<T>{
    protected final TokenService tokenService;
    private final OAuth2RestTemplate oAuth2RestTemplate;

    protected OAuth2CloudStorageConnectorGenericController(
            VirtualFileSystemService virtualFileSystemService,
            OAuth2RestTemplate oAuth2RestTemplate,
            TokenService tokenService) {
        super(virtualFileSystemService);
        this.oAuth2RestTemplate = oAuth2RestTemplate;
        this.tokenService = tokenService;
    }

    public void connect() throws IllegalAccessException {
        super.connect();
        updateToken();
    }

    private void updateToken() {
        Token token = new Token();
        token.setToken(oAuth2RestTemplate.getAccessToken().getValue());
        token.setRefreshToken(oAuth2RestTemplate.getAccessToken().getRefreshToken().getValue());
        token.setExpirationDate(oAuth2RestTemplate.getAccessToken().getExpiration());
        tokenService.update(token);
    }
}
