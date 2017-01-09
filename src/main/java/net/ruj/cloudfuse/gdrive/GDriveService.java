package net.ruj.cloudfuse.gdrive;

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
    private final OAuth2RestTemplate oAuth2RestTemplate;

    static final Logger logger = LoggerFactory.getLogger(GDriveService.class);

    @Autowired
    public GDriveService(OAuth2RestTemplate oAuth2RestTemplate) {
        this.oAuth2RestTemplate = oAuth2RestTemplate;
    }

    public List<String> list() throws URISyntaxException, UnsupportedEncodingException {
        FileList fileList = oAuth2RestTemplate.getForObject("https://www.googleapis.com/drive/v3/files?fields=files", FileList.class);
        return fileList.getFiles().stream().map(File::getName).collect(Collectors.toList());
    }
}
