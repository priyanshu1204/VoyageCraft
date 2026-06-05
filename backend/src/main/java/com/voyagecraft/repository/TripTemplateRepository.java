package com.voyagecraft.repository;

import com.voyagecraft.entity.TripTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripTemplateRepository extends JpaRepository<TripTemplate, Long> {
    List<TripTemplate> findByCategory(String category);
    List<TripTemplate> findAllByOrderByNameAsc();
}
