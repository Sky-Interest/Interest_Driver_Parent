package com.gec.interest.map.repository;

import com.gec.interest.model.entity.map.OrderServiceLocation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderServiceLocationRepository extends MongoRepository<OrderServiceLocation, String> {

    List<OrderServiceLocation> findByOrderIdByCreateTimeAsc(Long orderId);
}
