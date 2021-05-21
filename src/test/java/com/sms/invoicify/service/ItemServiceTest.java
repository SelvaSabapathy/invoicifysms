package com.sms.invoicify.service;

import com.sms.invoicify.exception.InvoicifyInvoiceNotExistsException;
import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.models.Item;
import com.sms.invoicify.models.ItemEntity;
import com.sms.invoicify.repository.ItemsRepositiory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ItemServiceTest {

  @Mock ItemsRepositiory itemsRepositiory;

  @Mock InvoiceService invoiceService;

  @InjectMocks ItemService itemService;

  @Test
  @DisplayName("Create new Item Test")
  void createItem() throws ParseException, InvoicifyInvoiceNotExistsException {
    Item itemDto =
        Item.builder()
            .description("MyInvoiceItem")
            .quantity(42)
            .totalFees(BigDecimal.valueOf(10.99))
            .invoiceNumber(120L)
            .build();

    ItemEntity itemEntity =
        ItemEntity.builder()
            .description("MyInvoiceItem")
            .quantity(42)
            .totalFees(BigDecimal.valueOf(10.99))
            .invoice(
                InvoiceEntity.builder()
                    .number(120L)
                    .lastModifiedDate(LocalDate.now())
                    .totalCost(BigDecimal.valueOf(10.99))
                    .build())
            .build();

    when(itemsRepositiory.save(any()))
        .thenReturn(
            ItemEntity.builder()
                .itemId(8L)
                .description("MyInvoiceItem")
                .quantity(42)
                .totalFees(BigDecimal.valueOf(10.99))
                .invoice(InvoiceEntity.builder().number(120L).build())
                .build());
    when(invoiceService.findByNumber(120L))
        .thenReturn(InvoiceEntity.builder().number(120L).totalCost(BigDecimal.ZERO).build());

    Long itemId = itemService.createItem(itemDto);

    verify(itemsRepositiory).save(itemEntity);
    verify(invoiceService).findByNumber(any());
    verifyNoMoreInteractions(invoiceService);
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
                    .invoice(InvoiceEntity.builder().number(120L).build())
                    .build()));

    List<Item> itemsFromService = itemService.fetchAllItems();

    Item itemDtoExpected =
        Item.builder()
            .description("MyInvoiceItem")
            .quantity(42)
            .totalFees(BigDecimal.valueOf(10.99))
            .invoiceNumber(120L)
            .build();

    assertThat(itemsFromService).isEqualTo(List.of(itemDtoExpected));

    verify(itemsRepositiory).findAll();
    verifyNoMoreInteractions(itemsRepositiory);
  }
}
