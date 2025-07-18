package com.lit.ims.controller;

import com.lit.ims.dto.CityDTO;
import com.lit.ims.dto.CountryDTO;
import com.lit.ims.dto.StateDTO;
import com.lit.ims.entity.City;
import com.lit.ims.entity.Country;
import com.lit.ims.entity.State;
import com.lit.ims.repository.CityRepository;
import com.lit.ims.repository.CountryRepository;
import com.lit.ims.repository.StateRepository;
import com.lit.ims.response.ApiResponse;
import com.lit.ims.service.LocationImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationImportController {

    private final LocationImportService locationImportService;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;


    @PostMapping("/india")
    public String triggerImport() {
        locationImportService.importIndiaOnly();
        return "Location import started";
    }
    @GetMapping("/countries")
    public ApiResponse<List<CountryDTO>> getAllCountries() {
        List<CountryDTO> countries = countryRepository.findAll().stream()
                .map(c -> new CountryDTO(c.getId(), c.getName(), c.getIso2()))
                .toList();
        return new ApiResponse<>(true, "Countries", countries);
    }

    @GetMapping("/states")
    public ApiResponse<List<StateDTO>> getStatesByCountry(@RequestParam Long countryId) {
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new RuntimeException("Country not found"));
        List<StateDTO> states = stateRepository.findByCountry(country).stream()
                .map(s -> new StateDTO(s.getId(), s.getName(), s.getIso2()))
                .toList();
        return new ApiResponse<>(true, "States", states);
    }

    @GetMapping("/cities")
    public ApiResponse<List<CityDTO>> getCitiesByState(@RequestParam Long stateId) {
        State state = stateRepository.findById(stateId)
                .orElseThrow(() -> new RuntimeException("State not found"));
        List<CityDTO> cities = cityRepository.findByState(state).stream()
                .map(c -> new CityDTO(c.getId(), c.getName()))
                .toList();
        return new ApiResponse<>(true, "Cities", cities);
    }


}
