package com.restroute.controller;

import com.restroute.service.image.RestStopImageCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/api/admin/rest-stops")
public class AdminRestStopImageController {

    private final RestStopImageCommandService commandService;

    @PutMapping(path = "/{serviceAreaCode}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> save(@PathVariable String serviceAreaCode, @RequestPart MultipartFile file) {
        commandService.save(serviceAreaCode, file);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{serviceAreaCode}/image")
    public ResponseEntity<Void> delete(@PathVariable String serviceAreaCode) {
        commandService.delete(serviceAreaCode);
        return ResponseEntity.noContent().build();
    }
}
