package com.examloop.api.controller;

import com.examloop.api.dto.ItemRequest;
import com.examloop.api.model.Item;
import com.examloop.api.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems(@RequestHeader("X-Device-Id") String deviceId) {
        return ResponseEntity.ok(itemService.getAllItems(deviceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id, @RequestHeader("X-Device-Id") String deviceId) {
        return itemService.getItemById(id, deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@RequestHeader("X-Device-Id") String deviceId,
                                          @Valid @RequestBody ItemRequest request) {
        Item item = itemService.createItem(deviceId, request.getGoalId(), 
                                          request.getQuestion(), request.getAnswer());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id,
                                          @RequestHeader("X-Device-Id") String deviceId,
                                          @Valid @RequestBody ItemRequest request) {
        return itemService.updateItem(id, deviceId, request.getQuestion(), request.getAnswer())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id, @RequestHeader("X-Device-Id") String deviceId) {
        return itemService.deleteItem(id, deviceId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
