package com.sms.invoicify.service;

import com.sms.invoicify.models.Item;
import com.sms.invoicify.models.ItemEntity;
import com.sms.invoicify.repository.ItemsRepositiory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

  private final ItemsRepositiory itemsRepositiory;

  public Long createItem(Item itemDto) {
    ItemEntity persisted =
        itemsRepositiory.save(
            ItemEntity.builder()
                .description(itemDto.getDescription())
                .quantity(itemDto.getQuantity())
                .totalFees(itemDto.getTotalFees())
                .build());
    return persisted.getItemId();
  }

  public List<Item> fetchAllItems() {
    return itemsRepositiory.findAll().stream()
        .map(
            itemEntity -> {
              return Item.builder()
                  .description(itemEntity.getDescription())
                  .quantity(itemEntity.getQuantity())
                  .totalFees(itemEntity.getTotalFees())
                  .build();
            })
        .collect(Collectors.toList());
  }
}
