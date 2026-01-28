package com.examloop.api.service;

import com.examloop.api.model.Goal;
import com.examloop.api.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GoalService {
    @Autowired
    private GoalRepository goalRepository;

    public List<Goal> getAllGoals(String deviceId) {
        return goalRepository.findByDeviceId(deviceId);
    }

    public Optional<Goal> getGoalById(Long id, String deviceId) {
        return goalRepository.findById(id)
                .filter(goal -> goal.getDeviceId().equals(deviceId));
    }

    public Goal createGoal(String deviceId, String title, String description) {
        Goal goal = new Goal();
        goal.setDeviceId(deviceId);
        goal.setTitle(title);
        goal.setDescription(description);
        return goalRepository.save(goal);
    }

    public Optional<Goal> updateGoal(Long id, String deviceId, String title, String description) {
        return goalRepository.findById(id)
                .filter(goal -> goal.getDeviceId().equals(deviceId))
                .map(goal -> {
                    goal.setTitle(title);
                    goal.setDescription(description);
                    return goalRepository.save(goal);
                });
    }

    public boolean deleteGoal(Long id, String deviceId) {
        return goalRepository.findById(id)
                .filter(goal -> goal.getDeviceId().equals(deviceId))
                .map(goal -> {
                    goalRepository.delete(goal);
                    return true;
                }).orElse(false);
    }
}
