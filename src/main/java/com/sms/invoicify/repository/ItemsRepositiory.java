package com.sms.invoicify.repository;

import com.sms.invoicify.models.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemsRepositiory extends JpaRepository<ItemEntity, Long> {}
