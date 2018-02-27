package net.ruj.cloudfuse.clouds;

import net.ruj.cloudfuse.clouds.exceptions.MakeRootException;
import net.ruj.cloudfuse.fuse.CloudFileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CloudStoragesSetupController {
    private CloudFileSystemService cloudFileSystemService;

    @Autowired
    public CloudStoragesSetupController(CloudFileSystemService cloudFileSystemService) {
        this.cloudFileSystemService = cloudFileSystemService;
    }

    @GetMapping("/mount")
    public void mount() throws IllegalAccessException, MakeRootException {
        cloudFileSystemService.init();
    }
}
