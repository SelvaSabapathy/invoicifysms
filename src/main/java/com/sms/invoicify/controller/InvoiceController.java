package com.sms.invoicify.controller;

import com.sms.invoicify.exception.InvoicifyCompanyNotExistsException;
import com.sms.invoicify.exception.InvoicifyInvoiceExistsException;
import com.sms.invoicify.exception.InvoicifyInvoiceNotExistsException;
import com.sms.invoicify.models.Invoice;
import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.models.InvoiceSummary;
import com.sms.invoicify.models.Item;
import com.sms.invoicify.models.ItemEntity;
import com.sms.invoicify.service.InvoiceService;
import com.sms.invoicify.utilities.PaymentStatus;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/invoices")
@AllArgsConstructor
@Validated
public class InvoiceController {

  private InvoiceService invoiceService;

  @PostMapping
  public ResponseEntity<Invoice> create(@Valid @RequestBody Invoice invoice) {
    List<Item> items = invoice.getItems();
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
            .number(invoice.getNumber())
            .creationDate(invoice.getCreationDate())
            .items(itemEntities)
            .companyName(invoice.getCompanyName())
            .paymentStatus(invoice.getPaymentStatus())
            .totalCost(invoice.getTotalCost())
            .build();

    InvoiceEntity createdInvoiceEntity = null;
    try {
      createdInvoiceEntity = invoiceService.create(invoiceEntity);
    } catch (InvoicifyInvoiceExistsException | InvoicifyCompanyNotExistsException e) {
      return new ResponseEntity<Invoice>(new Invoice(), HttpStatus.BAD_REQUEST);
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

    Invoice createdInvoice =
        Invoice.builder()
            .number(createdInvoiceEntity.getNumber())
            .creationDate(createdInvoiceEntity.getCreationDate())
            .items(retItems)
            .companyName(createdInvoiceEntity.getCompanyName())
            .paymentStatus(createdInvoiceEntity.getPaymentStatus())
            .totalCost(createdInvoiceEntity.getTotalCost())
            .build();

    return new ResponseEntity<Invoice>(createdInvoice, HttpStatus.CREATED);
  }

  @GetMapping("/summary")
  public ResponseEntity<List<InvoiceSummary>> getInvoicesSummary(
      @RequestParam(defaultValue = "0") Integer pageNumber,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    List<InvoiceSummary> summaryDtoList =
        invoiceService.viewAllinvoices(pageNumber, pageSize).stream()
            .map(
                e ->
                    new InvoiceSummary(
                        e.getNumber(), e.getCreationDate(), e.getPaymentStatus(), e.getTotalCost()))
            .collect(Collectors.toList());
    return new ResponseEntity<>(summaryDtoList, HttpStatus.OK);
  }

  @GetMapping
  public ResponseEntity<List<Invoice>> getInvoices(
      @RequestParam(defaultValue = "0") Integer pageNumber,
      @RequestParam(defaultValue = "10") Integer pageSize) {

    List<Invoice> dtos =
        invoiceService.viewAllinvoices(pageNumber, pageSize).stream()
            .map(
                e ->
                    new Invoice(
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

  @GetMapping("/search/{number}")
  public ResponseEntity<List<Invoice>> searchInvoices(@PathVariable("number") Long number) {

    List<Invoice> dtos =
        List.of(invoiceService.findByNumber(number)).stream()
            .map(
                e ->
                    new Invoice(
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

  @PutMapping
  public ResponseEntity<Invoice> update(@Valid @RequestBody Invoice invoice) {
    List<Item> items = invoice.getItems();
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
            .number(invoice.getNumber())
            .creationDate(invoice.getCreationDate())
            .items(itemEntities)
            .companyName(invoice.getCompanyName())
            .paymentStatus(invoice.getPaymentStatus())
            .totalCost(invoice.getTotalCost())
            .build();
    try {
      invoiceService.update(invoiceEntity);
    } catch (InvoicifyInvoiceNotExistsException | ParseException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping
  public ResponseEntity deleteInvoices() {
    invoiceService.delete();
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/unpaid/{companyName}")
  public ResponseEntity<List<Invoice>> getUnpaidInvoices(
      @PathVariable("companyName") String companyName,
      @RequestParam(defaultValue = "0") Integer pageNumber,
      @RequestParam(defaultValue = "10") Integer pageSize) {

    List<Invoice> dtos =
        invoiceService
            .findByCompanyNameAndPaymentStatus(
                companyName, PaymentStatus.UNPAID, pageNumber, pageSize)
            .stream()
            .map(
                e ->
                    new Invoice(
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

  @GetMapping("/summary/unpaid/{companyName}")
  public ResponseEntity<List<InvoiceSummary>> getUnpaidInvoicesSummary(
      @PathVariable("companyName") String companyName,
      @RequestParam(defaultValue = "0") Integer pageNumber,
      @RequestParam(defaultValue = "10") Integer pageSize) {

    List<InvoiceSummary> dtos =
        invoiceService
            .findByCompanyNameAndPaymentStatus(
                companyName, PaymentStatus.UNPAID, pageNumber, pageSize)
            .stream()
            .map(
                e ->
                    new InvoiceSummary(
                        e.getNumber(), e.getCreationDate(), e.getPaymentStatus(), e.getTotalCost()))
            .collect(Collectors.toList());
    return new ResponseEntity<>(dtos, HttpStatus.OK);
  }
}
