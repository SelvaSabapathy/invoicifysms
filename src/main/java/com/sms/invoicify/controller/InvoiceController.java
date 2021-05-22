package com.sms.invoicify.controller;

import com.sms.invoicify.exception.InvoicifyCompanyNotExistsException;
import com.sms.invoicify.exception.InvoicifyInvoiceExistsException;
import com.sms.invoicify.exception.InvoicifyInvoiceNotExistsException;
import com.sms.invoicify.models.InvoiceDto;
import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.models.InvoiceSummaryDto;
import com.sms.invoicify.models.Item;
import com.sms.invoicify.models.ItemEntity;
import com.sms.invoicify.service.InvoiceService;
import com.sms.invoicify.utilities.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.ParseException;
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

    InvoiceEntity createdInvoiceEntity = null;
    try {
      createdInvoiceEntity = invoiceService.create(invoiceEntity);
    } catch (InvoicifyInvoiceExistsException | InvoicifyCompanyNotExistsException e) {
      return new ResponseEntity<InvoiceDto>(new InvoiceDto(), HttpStatus.BAD_REQUEST);
    }

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

  @GetMapping("/invoices/summary")
  public ResponseEntity<List<InvoiceSummaryDto>> getInvoicesSummary() {
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

  @GetMapping("/invoices/search/{number}")
  public ResponseEntity<List<InvoiceDto>> searchInvoices(@PathVariable("number") Long number) {

    List<InvoiceDto> dtos =
        List.of(invoiceService.findByNumber(number)).stream()
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

  @PutMapping("/invoices")
  public ResponseEntity<InvoiceDto> update(@Valid @RequestBody InvoiceDto invoiceDto) {
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
    try {
      invoiceService.update(invoiceEntity);
    } catch (InvoicifyInvoiceNotExistsException | ParseException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/invoices")
  public ResponseEntity deleteInvoices() {
    invoiceService.delete();
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/invoices/unpaid/{companyName}")
  public ResponseEntity<List<InvoiceDto>> getUnpaidInvoices(
      @PathVariable("companyName") String companyName) {

    List<InvoiceDto> dtos =
        invoiceService.findByCompanyNameAndPaymentStatusOrderByCreationDateAsc(companyName, PaymentStatus.UNPAID).stream()
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

  @GetMapping("/invoices/summary/unpaid/{companyName}")
  public ResponseEntity<List<InvoiceSummaryDto>> getUnpaidInvoicesSummary(@PathVariable("companyName") String companyName) {

    List<InvoiceSummaryDto> dtos =
        invoiceService.findByCompanyNameAndPaymentStatusOrderByCreationDateAsc(companyName, PaymentStatus.UNPAID).stream()
            .map(
                e ->
                    new InvoiceSummaryDto(
                        e.getNumber(), e.getCreationDate(), e.getPaymentStatus(), e.getTotalCost()))
            .collect(Collectors.toList());
    return new ResponseEntity<>(dtos, HttpStatus.OK);
  }
}
