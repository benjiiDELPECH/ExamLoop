package com.examloop.api.service;

import com.examloop.api.model.Item;
import com.examloop.api.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    public List<Item> getAllItems(String deviceId) {
        return itemRepository.findByDeviceId(deviceId);
    }

    public Optional<Item> getItemById(Long id, String deviceId) {
        return itemRepository.findById(id)
                .filter(item -> item.getDeviceId().equals(deviceId));
    }

    public Item createItem(String deviceId, Long goalId, String question, String answer) {
        Item item = new Item();
        item.setDeviceId(deviceId);
        item.setGoalId(goalId);
        item.setQuestion(question);
        item.setAnswer(answer);
        item.setBox(1);
        item.setNextReview(LocalDateTime.now());
        return itemRepository.save(item);
    }

    public Optional<Item> updateItem(Long id, String deviceId, String question, String answer) {
        return itemRepository.findById(id)
                .filter(item -> item.getDeviceId().equals(deviceId))
                .map(item -> {
                    item.setQuestion(question);
                    item.setAnswer(answer);
                    return itemRepository.save(item);
                });
    }

    public boolean deleteItem(Long id, String deviceId) {
        return itemRepository.findById(id)
                .filter(item -> item.getDeviceId().equals(deviceId))
                .map(item -> {
                    itemRepository.delete(item);
                    return true;
                }).orElse(false);
    }

    public List<Item> getTodayItems(String deviceId) {
        return itemRepository.findDueItems(deviceId, LocalDateTime.now());
    }

    public Optional<Item> reviewItem(Long id, String deviceId, boolean correct) {
        return itemRepository.findById(id)
                .filter(item -> item.getDeviceId().equals(deviceId))
                .map(item -> {
                    int currentBox = item.getBox();
                    if (correct) {
                        // Move to next box (max 5)
                        item.setBox(Math.min(currentBox + 1, 5));
                        // Calculate next review based on Leitner system
                        item.setNextReview(calculateNextReview(item.getBox()));
                    } else {
                        // Move back to box 1
                        item.setBox(1);
                        item.setNextReview(LocalDateTime.now().plusDays(1));
                    }
                    return itemRepository.save(item);
                });
    }

    private LocalDateTime calculateNextReview(int box) {
        // Leitner system intervals
        return switch (box) {
            case 1 -> LocalDateTime.now().plusDays(1);
            case 2 -> LocalDateTime.now().plusDays(3);
            case 3 -> LocalDateTime.now().plusDays(7);
            case 4 -> LocalDateTime.now().plusDays(14);
            case 5 -> LocalDateTime.now().plusDays(30);
            default -> LocalDateTime.now().plusDays(1);
        };
    }
}
