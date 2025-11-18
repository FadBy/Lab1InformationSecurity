package com.infosec.secureapi.controller;

import com.infosec.secureapi.dto.DataItemRequest;
import com.infosec.secureapi.dto.DataItemResponse;
import com.infosec.secureapi.entity.User;
import com.infosec.secureapi.service.DataService;
import com.infosec.secureapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")
public class DataController {
    private final DataService dataService;
    private final UserService userService;

    @Autowired
    public DataController(DataService dataService, UserService userService) {
        this.dataService = dataService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<DataItemResponse>> getAllDataItems() {
        List<DataItemResponse> items = dataService.getAllDataItems();
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<DataItemResponse> createDataItem(
            @Valid @RequestBody DataItemRequest request,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername());
        
        DataItemResponse response = dataService.createDataItem(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

