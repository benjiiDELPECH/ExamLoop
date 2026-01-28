package com.examloop.api.controller;

import com.examloop.api.dto.ReviewRequest;
import com.examloop.api.model.Item;
import com.examloop.api.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private ItemService itemService;

    @GetMapping("/today")
    public ResponseEntity<List<Item>> getTodayItems(@RequestHeader("X-Device-Id") String deviceId) {
        return ResponseEntity.ok(itemService.getTodayItems(deviceId));
    }
}
