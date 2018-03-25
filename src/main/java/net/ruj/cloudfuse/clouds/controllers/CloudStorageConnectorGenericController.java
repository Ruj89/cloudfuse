package net.ruj.cloudfuse.clouds.controllers;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.clouds.exceptions.MakeRootException;
import net.ruj.cloudfuse.database.models.Token;
import net.ruj.cloudfuse.database.services.TokenService;
import net.ruj.cloudfuse.fuse.VirtualFileSystemService;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;

public abstract class CloudStorageConnectorGenericController<T extends CloudStorageService> {
    protected final VirtualFileSystemService virtualFileSystemService;

    protected CloudStorageConnectorGenericController(VirtualFileSystemService virtualFileSystemService) {
        this.virtualFileSystemService = virtualFileSystemService;
    }

    public void connect() throws IllegalAccessException {
        virtualFileSystemService.addCloudStorageService(buildCloudStorageService());
    }

    protected abstract T buildCloudStorageService();
}
