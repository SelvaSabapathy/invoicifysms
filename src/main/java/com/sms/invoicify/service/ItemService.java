package com.sms.invoicify.service;

import com.sms.invoicify.models.Item;
import com.sms.invoicify.models.ItemEntity;
import com.sms.invoicify.repository.ItemsRepositiory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

  private final ItemsRepositiory itemsRepositiory;

  public void createItem(Item itemDto) {
    itemsRepositiory.save(ItemEntity.builder()
            .description(itemDto.getDescription())
            .quantity(itemDto.getQuantity())
            .totalFees(itemDto.getTotalFees())
            .build());
  }
}
