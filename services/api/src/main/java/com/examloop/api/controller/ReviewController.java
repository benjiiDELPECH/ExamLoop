package com.examloop.api.controller;

import com.examloop.api.dto.ReviewRequest;
import com.examloop.api.model.Item;
import com.examloop.api.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ItemService itemService;

    @PostMapping("/{id}")
    public ResponseEntity<Item> reviewItem(@PathVariable Long id,
                                          @RequestHeader("X-Device-Id") String deviceId,
                                          @Valid @RequestBody ReviewRequest request) {
        return itemService.reviewItem(id, deviceId, request.getCorrect())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
