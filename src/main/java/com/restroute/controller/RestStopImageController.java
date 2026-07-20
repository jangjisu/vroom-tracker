package com.restroute.controller;

import com.restroute.service.image.RestStopImageQueryService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/api/rest-stops")
public class RestStopImageController {

    private static final MediaType WEBP_MEDIA_TYPE = MediaType.parseMediaType("image/webp");
    private static final String CACHE_CONTROL_VALUE = "public, no-cache";

    private final RestStopImageQueryService queryService;

    @GetMapping("/{serviceAreaCode}/images/detail")
    public ResponseEntity<byte[]> getDetailImage(
            @PathVariable String serviceAreaCode,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        return imageResponse(queryService.findDetailImage(serviceAreaCode), ifNoneMatch);
    }

    @GetMapping("/{serviceAreaCode}/images/list")
    public ResponseEntity<byte[]> getListImage(
            @PathVariable String serviceAreaCode,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        return imageResponse(queryService.findListImage(serviceAreaCode), ifNoneMatch);
    }

    private ResponseEntity<byte[]> imageResponse(Optional<byte[]> image, String ifNoneMatch) {
        if (image.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        byte[] imageData = image.get();
        String eTag = "\"" + DigestUtils.md5DigestAsHex(imageData) + "\"";
        if (eTag.equals(ifNoneMatch)) {
            return ResponseEntity.status(304)
                    .eTag(eTag)
                    .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_VALUE)
                    .build();
        }
        return ResponseEntity.ok()
                .contentType(WEBP_MEDIA_TYPE)
                .eTag(eTag)
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_VALUE)
                .body(imageData);
    }
}
