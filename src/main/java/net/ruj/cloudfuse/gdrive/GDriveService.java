package net.ruj.cloudfuse.gdrive;

import net.ruj.cloudfuse.gdrive.models.File;
import net.ruj.cloudfuse.gdrive.models.FileList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GDriveService {
    static final Logger logger = LoggerFactory.getLogger(GDriveService.class);
    private final OAuth2RestTemplate oAuth2RestTemplate;

    @Autowired
    public GDriveService(OAuth2RestTemplate oAuth2RestTemplate) {
        this.oAuth2RestTemplate = oAuth2RestTemplate;
    }

    public List<String> list() throws URISyntaxException, UnsupportedEncodingException {
        FileList fileList = oAuth2RestTemplate.getForObject("https://www.googleapis.com/drive/v3/files?fields=files", FileList.class);
        return fileList.getFiles().stream().map(File::getName).collect(Collectors.toList());
    }
}
