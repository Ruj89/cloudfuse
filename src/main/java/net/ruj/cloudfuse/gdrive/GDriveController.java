package net.ruj.cloudfuse.gdrive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
public class GDriveController {
    private final GDriveService gDriveService;

    @Autowired
    public GDriveController(GDriveService gDriveService) {
        this.gDriveService = gDriveService;
    }

    @GetMapping("/list")
    private List<String> list() throws URISyntaxException, UnsupportedEncodingException {
        return gDriveService.list();
    }
}
