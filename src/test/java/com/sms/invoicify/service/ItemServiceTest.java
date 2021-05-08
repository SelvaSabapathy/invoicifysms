package com.sms.invoicify.service;

import com.sms.invoicify.models.Item;
import com.sms.invoicify.models.ItemEntity;
import com.sms.invoicify.repository.ItemsRepositiory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

  @Mock ItemsRepositiory itemsRepositiory;

  @InjectMocks ItemService itemService;

  @Test
  @DisplayName("Create new Item Test")
  void createItem() {
    Item itemDto =
        Item.builder()
            .description("MyInvoiceItem")
            .quantity(42)
            .totalFees(BigDecimal.valueOf(10.99))
            .build();

    ItemEntity itemEntity =
        ItemEntity.builder()
            .description("MyInvoiceItem")
            .quantity(42)
            .totalFees(BigDecimal.valueOf(10.99))
            .build();

    when(itemsRepositiory.save(any()))
        .thenReturn(
            ItemEntity.builder()
                .itemId(8L)
                .description("MyInvoiceItem")
                .quantity(42)
                .totalFees(BigDecimal.valueOf(10.99))
                .build());

    Long itemId = itemService.createItem(itemDto);
    verify(itemsRepositiory).save(itemEntity);
    verifyNoMoreInteractions(itemsRepositiory);
    assertEquals(8L, itemId);
  }

  @Test
  @DisplayName("Get All Items")
  void getAllItems() {
    when(itemsRepositiory.findAll())
        .thenReturn(
            List.of(
                ItemEntity.builder()
                    .itemId(8L)
                    .description("MyInvoiceItem")
                    .quantity(42)
                    .totalFees(BigDecimal.valueOf(10.99))
                    .build()));

    List<Item> itemsFromService = itemService.fetchAllItems();

    Item itemDtoExpected =
        Item.builder()
            .description("MyInvoiceItem")
            .quantity(42)
            .totalFees(BigDecimal.valueOf(10.99))
            .build();

    assertThat(itemsFromService).isEqualTo(List.of(itemDtoExpected));

    verify(itemsRepositiory).findAll();
    verifyNoMoreInteractions(itemsRepositiory);
  }
}
