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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    ItemsRepositiory itemsRepositiory;

    @InjectMocks
    ItemService itemService;

  @Test
  @DisplayName("Create new Item Test")
  void createItem() {
      Item itemDto = Item.builder()
              .description("MyInvoiceItem")
              .quantity(42)
              .totalFees(BigDecimal.valueOf(10.99))
              .build();

    itemService.createItem(itemDto);

   verify(itemsRepositiory).save(
           ItemEntity.builder()
                   .description("MyInvoiceItem")
                   .quantity(42)
                   .totalFees(BigDecimal.valueOf(10.99))
                   .build()
   );
   verifyNoMoreInteractions(itemsRepositiory);
  }
}
