package com.examloop.api.repository;

import com.examloop.api.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByDeviceId(String deviceId);
    List<Item> findByGoalId(Long goalId);
    
    @Query("SELECT i FROM Item i WHERE i.deviceId = :deviceId AND i.nextReview <= :now ORDER BY i.nextReview ASC")
    List<Item> findDueItems(@Param("deviceId") String deviceId, @Param("now") LocalDateTime now);
}
