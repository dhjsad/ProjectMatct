package com.projectmatch.controller;

import com.projectmatch.security.SecurityUtils;
import com.projectmatch.service.DownloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class DownloadController {

    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @GetMapping("/{id}/download/tutorial")
    public ResponseEntity<byte[]> tutorial(@PathVariable Long id) {
        return downloadService.tutorial(id);
    }

    @GetMapping("/{id}/download/source")
    public ResponseEntity<byte[]> source(@PathVariable Long id) {
        return downloadService.sourcePack(id, SecurityUtils.currentUserId());
    }

    @GetMapping("/{id}/resources/{resourceId}/download")
    public ResponseEntity<byte[]> resource(@PathVariable Long id, @PathVariable Long resourceId) {
        return downloadService.resourceFile(id, resourceId, SecurityUtils.currentUserId());
    }
}
