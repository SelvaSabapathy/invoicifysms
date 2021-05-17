package com.sms.invoicify.service;

import com.sms.invoicify.models.InvoiceDto;
import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.models.Item;
import com.sms.invoicify.models.ItemEntity;
import com.sms.invoicify.repository.ItemsRepositiory;
import com.sms.invoicify.utilities.InvoicifyUtilities;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ItemService {

  private final ItemsRepositiory itemsRepository;
  private InvoiceService invoiceService;

  public Long createItem(Item itemDto) throws ParseException {
    InvoiceDto invoiceDto = itemDto.getInvoice();

    // verify whether invoice exists
    InvoiceEntity invoiceEntity = invoiceService.findByNumber(invoiceDto.getNumber());
    if (invoiceEntity == null) {
      invoiceEntity =
          InvoiceEntity.builder()
              .number(invoiceDto.getNumber())
              .creationDate(invoiceDto.getCreationDate())
              .lastModifiedDate(invoiceDto.getLastModifiedDate())
              .companyName(invoiceDto.getCompanyName())
              .paymentStatus(invoiceDto.getPaymentStatus())
              .totalCost(itemDto.getTotalFees())
              .build();
    } else {
      invoiceEntity.setLastModifiedDate(LocalDate.now());
    }

    ItemEntity persisted =
        itemsRepository.save(
            ItemEntity.builder()
                .description(itemDto.getDescription())
                .quantity(itemDto.getQuantity())
                .totalFees(itemDto.getTotalFees())
                .invoice(invoiceEntity)
                .build());
    return persisted.getItemId();
  }

  private InvoiceDto convertFrom(InvoiceEntity invoiceEntity) {
    return InvoiceDto.builder()
        .number(invoiceEntity.getNumber())
        .creationDate(invoiceEntity.getCreationDate())
        .lastModifiedDate(invoiceEntity.getLastModifiedDate())
        .companyName(invoiceEntity.getCompanyName())
        .paymentStatus(invoiceEntity.getPaymentStatus())
        .totalCost(invoiceEntity.getTotalCost())
        .build();
  }

  public List<Item> fetchAllItems() {
    return itemsRepository.findAll().stream()
        .map(
            itemEntity -> {
              return Item.builder()
                  .description(itemEntity.getDescription())
                  .quantity(itemEntity.getQuantity())
                  .totalFees(itemEntity.getTotalFees())
                  .invoice(convertFrom(itemEntity.getInvoice()))
                  .build();
            })
        .collect(Collectors.toList());
  }
}
