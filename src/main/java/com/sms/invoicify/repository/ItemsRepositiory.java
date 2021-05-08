package com.sms.invoicify.repository;

import com.sms.invoicify.models.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemsRepositiory extends JpaRepository<ItemEntity, Long> {}
