package com.sms.invoicify.service;

import com.sms.invoicify.exception.InvoicifyInvoiceNotExistsException;
import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.models.Item;
import com.sms.invoicify.models.ItemEntity;
import com.sms.invoicify.repository.ItemsRepositiory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ItemService {

  private final ItemsRepositiory itemsRepository;
  private InvoiceService invoiceService;

  public Long createItem(Item itemDto) throws InvoicifyInvoiceNotExistsException {

    // verify whether invoice exists
    InvoiceEntity invoiceEntity = invoiceService.findByNumber(itemDto.getInvoiceNumber());
    if (invoiceEntity == null) {
      throw new InvoicifyInvoiceNotExistsException(
          "Invoice does not exist, item cannot be created");
    } else {
      invoiceEntity.setLastModifiedDate(LocalDate.now());
      invoiceEntity.setTotalCost(invoiceEntity.getTotalCost().add(itemDto.getTotalFees()));
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

  public List<Item> fetchAllItems() {
    return itemsRepository.findAll().stream()
        .map(
            itemEntity -> {
              return Item.builder()
                  .description(itemEntity.getDescription())
                  .quantity(itemEntity.getQuantity())
                  .totalFees(itemEntity.getTotalFees())
                  .invoiceNumber(itemEntity.getInvoice().getNumber())
                  .build();
            })
        .collect(Collectors.toList());
  }
}
