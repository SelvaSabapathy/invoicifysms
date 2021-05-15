package com.sms.invoicify.controller;

import com.sms.invoicify.models.InvoiceDto;
import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.models.InvoiceSummaryDto;
import com.sms.invoicify.models.Item;
import com.sms.invoicify.models.ItemEntity;
import com.sms.invoicify.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
public class InvoiceController {

  @Autowired private InvoiceService invoiceService;

  @PostMapping("/invoices")
  public ResponseEntity<InvoiceDto> create(@Valid @RequestBody InvoiceDto invoiceDto) {
    List<Item> items = invoiceDto.getItems();
    List<ItemEntity> itemEntities =
        items == null
            ? null
            : items.stream()
                .map(
                    e ->
                        ItemEntity.builder()
                            .description(e.getDescription())
                            .quantity(e.getQuantity())
                            .totalFees(e.getTotalFees())
                            .build())
                .collect(Collectors.toList());

    InvoiceEntity invoiceEntity =
        InvoiceEntity.builder()
            .number(invoiceDto.getNumber())
            .creationDate(invoiceDto.getCreationDate())
            .items(itemEntities)
            .companyName(invoiceDto.getCompanyName())
            .paymentStatus(invoiceDto.getPaymentStatus())
            .totalCost(invoiceDto.getTotalCost())
            .build();
    InvoiceEntity createdInvoiceEntity = invoiceService.create(invoiceEntity);

    List<ItemEntity> retItemEnt = createdInvoiceEntity.getItems();
    List<Item> retItems =
        retItemEnt == null
            ? null
            : retItemEnt.stream()
                .map(
                    e ->
                        Item.builder()
                            .description(e.getDescription())
                            .quantity(e.getQuantity())
                            .totalFees(e.getTotalFees())
                            .build())
                .collect(Collectors.toList());

    InvoiceDto createdInvoiceDto =
        InvoiceDto.builder()
            .number(createdInvoiceEntity.getNumber())
            .creationDate(createdInvoiceEntity.getCreationDate())
            .items(retItems)
            .companyName(createdInvoiceEntity.getCompanyName())
            .paymentStatus(createdInvoiceEntity.getPaymentStatus())
            .totalCost(createdInvoiceEntity.getTotalCost())
            .build();

    return new ResponseEntity<InvoiceDto>(createdInvoiceDto, HttpStatus.CREATED);
  }

  @GetMapping("/invoices/{type}")
  public ResponseEntity<List<InvoiceSummaryDto>> getInvoices(@PathParam("type") String type) {
    List<InvoiceSummaryDto> summaryDtoList =
        invoiceService.view().stream()
            .map(
                e ->
                    new InvoiceSummaryDto(
                        e.getNumber(), e.getCreationDate(), e.getPaymentStatus(), e.getTotalCost()))
            .collect(Collectors.toList());
    return new ResponseEntity<>(summaryDtoList, HttpStatus.OK);
  }

  @GetMapping("/invoices")
  public ResponseEntity<List<InvoiceDto>> getInvoices() {

    List<InvoiceDto> dtos =
        invoiceService.view().stream()
            .map(
                e ->
                    new InvoiceDto(
                        e.getNumber(),
                        e.getCreationDate(),
                        e.getLastModifiedDate(),
                        getItemDtos(e.getItems()),
                        e.getCompanyName(),
                        e.getPaymentStatus(),
                        e.getTotalCost()))
            .collect(Collectors.toList());
    return new ResponseEntity<>(dtos, HttpStatus.OK);
  }

  private List<Item> getItemDtos(List<ItemEntity> itemEntities) {

    return itemEntities == null
        ? null
        : itemEntities.stream()
            .map(
                e ->
                    Item.builder()
                        .description(e.getDescription())
                        .quantity(e.getQuantity())
                        .totalFees(e.getTotalFees())
                        .build())
            .collect(Collectors.toList());
  }
}
