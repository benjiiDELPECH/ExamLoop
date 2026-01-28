package com.examloop.api.controller;

import com.examloop.api.dto.GoalRequest;
import com.examloop.api.model.Goal;
import com.examloop.api.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @GetMapping
    public ResponseEntity<List<Goal>> getAllGoals(@RequestHeader("X-Device-Id") String deviceId) {
        return ResponseEntity.ok(goalService.getAllGoals(deviceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Goal> getGoal(@PathVariable Long id, @RequestHeader("X-Device-Id") String deviceId) {
        return goalService.getGoalById(id, deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Goal> createGoal(@RequestHeader("X-Device-Id") String deviceId, 
                                          @Valid @RequestBody GoalRequest request) {
        Goal goal = goalService.createGoal(deviceId, request.getTitle(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(goal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Goal> updateGoal(@PathVariable Long id,
                                          @RequestHeader("X-Device-Id") String deviceId,
                                          @Valid @RequestBody GoalRequest request) {
        return goalService.updateGoal(id, deviceId, request.getTitle(), request.getDescription())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id, @RequestHeader("X-Device-Id") String deviceId) {
        return goalService.deleteGoal(id, deviceId) 
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
