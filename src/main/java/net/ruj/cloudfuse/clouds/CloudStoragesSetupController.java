package net.ruj.cloudfuse.clouds;

import net.ruj.cloudfuse.fuse.VirtualFileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CloudStoragesSetupController {
    private VirtualFileSystemService virtualFileSystemService;

    @Autowired
    public CloudStoragesSetupController(VirtualFileSystemService virtualFileSystemService) {
        this.virtualFileSystemService = virtualFileSystemService;
    }

    @GetMapping("/mount")
    public void mount() throws IllegalAccessException {
        virtualFileSystemService.init();
    }
}
