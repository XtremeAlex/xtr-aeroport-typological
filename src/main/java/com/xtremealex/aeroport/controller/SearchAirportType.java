package com.xtremealex.aeroport.controller;

import com.xtremealex.aeroport.models.web.ErrorCode;
import com.xtremealex.aeroport.models.web.response.ResponseWrapper;
import com.xtremealex.aeroport.models.web.response.ResponseWrapperBuilder;
import com.xtremealex.aeroport.models.web.response.airports.AirportTypeDTO;
import com.xtremealex.aeroport.service.IAirportTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Search Airport Type", description = "Tipologie di aeroporti")
public class SearchAirportType {

    @Autowired
    private IAirportTypeService airportService;

    @Autowired
    private ResponseWrapperBuilder responseWrapperBuilder;

    @GetMapping("/getAllAirportTypes")
    public ResponseEntity<ResponseWrapper<Page<AirportTypeDTO>>> getAirportsZero() {
        try {

            Page page = airportService.getAll(creaPaginazione(0, 12, null, "name"));

            if (page == null || page.isEmpty()) {
                return new ResponseEntity<>(responseWrapperBuilder.buildResponse(ErrorCode.E1, null, "Nessun aeroporto trovato"), null, HttpStatus.NOT_FOUND);
            }

            return returnResults(page, null);

        } catch (Exception e) {
            return new ResponseEntity<>(responseWrapperBuilder.buildResponse(ErrorCode.E0, null, e.getMessage()), null, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @GetMapping("/getAllAirportType/{pageNumber}/{pageSize}/{sortField}/{sortDir}")
    public ResponseEntity<ResponseWrapper<Page<AirportTypeDTO>>> getAirportsByPathVariable(@PathVariable Integer pageNumber,
                                         @PathVariable Integer pageSize,
                                         @PathVariable String sortField,
                                         @PathVariable String sortDir) {

        try {

            Page page = airportService.getAll(creaPaginazione(pageNumber, pageSize, sortField, sortDir));

            if (page == null || page.isEmpty()) {
                return new ResponseEntity<>(responseWrapperBuilder.buildResponse(ErrorCode.E1, null, "Nessun aeroporto trovato"), null, HttpStatus.NOT_FOUND);
            }

            return returnResults(page, null);

        } catch (Exception e) {
            //return returnError(e, null);
            return new ResponseEntity<>(responseWrapperBuilder.buildResponse(ErrorCode.E0, null, e.getMessage()), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllAirportType")
    public ResponseEntity<ResponseWrapper<Page<AirportTypeDTO>>> getAirportsByReqyestParam(@RequestParam(defaultValue = "0") Integer pageNumber,
                                          @RequestParam(defaultValue = "12") Integer pageSize,
                                          @RequestParam(required = false) String sortField,
                                          @RequestParam(defaultValue = "ASC") String sortDir) {

        try {

            Page page = airportService.getAll(creaPaginazione(pageNumber, pageSize, sortField, sortDir));

            if (page == null || page.isEmpty()) {
                return new ResponseEntity<>(responseWrapperBuilder.buildResponse(ErrorCode.E1, null, "Nessun aeroporto trovato"), null, HttpStatus.NOT_FOUND);
            }

            return returnResults(page, null);

        } catch (Exception e) {
            return new ResponseEntity<>(responseWrapperBuilder.buildResponse(ErrorCode.E0, null, e.getMessage()), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private Pageable creaPaginazione(int pageNumber, int pageSize, String sortField, String sortDir) {
        Pageable pageable;
        if (sortField != null && !sortField.isEmpty()) {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }
        return pageable;
    }


    private ResponseEntity<ResponseWrapper<Page<AirportTypeDTO>>> returnError(Exception e, Object searchParams) {
        return new ResponseEntity<>(responseWrapperBuilder.buildResponse(ErrorCode.E0, searchParams, e.getMessage()), null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ResponseWrapper<Page<AirportTypeDTO>>> returnResults(Page<Object> airports, Object searchParams) {
        if (airports == null || airports.isEmpty()) {
            return new ResponseEntity<>(responseWrapperBuilder.buildResponse(ErrorCode.E1, searchParams, "Nessun aeroporto trovato"), null, HttpStatus.OK);
        }
        return new ResponseEntity<>(responseWrapperBuilder.buildResponse(ErrorCode.E1, searchParams, airports), null, HttpStatus.OK);
    }
}
