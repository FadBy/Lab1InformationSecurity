package com.infosec.secureapi.repository;

import com.infosec.secureapi.entity.DataItem;
import com.infosec.secureapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataItemRepository extends JpaRepository<DataItem, Long> {
    List<DataItem> findByUser(User user);
    List<DataItem> findAllByOrderByCreatedAtDesc();
}

