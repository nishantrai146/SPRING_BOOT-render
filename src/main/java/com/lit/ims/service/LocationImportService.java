package com.lit.ims.service;

import com.lit.ims.entity.City;
import com.lit.ims.entity.Country;
import com.lit.ims.entity.State;
import com.lit.ims.repository.CityRepository;
import com.lit.ims.repository.CountryRepository;
import com.lit.ims.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class LocationImportService {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    private final String API_KEY = "ODczT25tbG54eWhVQWVvN1JaVHp5Y3pCZENyNk96YTlkQlNqeXhVTw==";
    private final String BASE_URL = "https://api.countrystatecity.in/v1";

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CSCAPI-KEY", API_KEY);
        return headers;
    }

    public void importIndiaOnly() {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());

        String countryCode = "IN";

        // 1. Fetch India info
        ResponseEntity<Map<String, Object>> countryResponse = restTemplate.exchange(
                BASE_URL + "/countries/" + countryCode,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> countryMap = countryResponse.getBody();
        if (countryMap == null) return;

        Country country = new Country();
        country.setIso2((String) countryMap.get("iso2"));
        country.setName((String) countryMap.get("name"));
        countryRepository.save(country);

        // 2. Fetch States of India
        ResponseEntity<List<Map<String, Object>>> stateResponse = restTemplate.exchange(
                BASE_URL + "/countries/" + countryCode + "/states",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        for (Map<String, Object> stateMap : stateResponse.getBody()) {
            String stateCode = (String) stateMap.get("iso2");
            String stateName = (String) stateMap.get("name");

            State state = new State();
            state.setIso2(stateCode);
            state.setName(stateName);
            state.setCountry(country);
            stateRepository.save(state);

            // 3. Fetch Cities of that State
            ResponseEntity<List<Map<String, Object>>> cityResponse = restTemplate.exchange(
                    BASE_URL + "/countries/" + countryCode + "/states/" + stateCode + "/cities",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            for (Map<String, Object> cityMap : cityResponse.getBody()) {
                City city = new City();
                city.setName((String) cityMap.get("name"));
                city.setState(state);
                city.setCountry(country);
                cityRepository.save(city);
            }
        }
    }
}
