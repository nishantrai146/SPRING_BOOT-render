package com.lit.ims.controller;

import com.lit.ims.service.LocationImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location-import")
@RequiredArgsConstructor
public class LocationImportController {

    private final LocationImportService locationImportService;

    @PostMapping("/india")
    public String triggerImport() {
        locationImportService.importIndiaOnly();
        return "Location import started";
    }
}
