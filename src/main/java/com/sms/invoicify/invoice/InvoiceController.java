package com.sms.invoicify.invoice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvoiceController {

  @Autowired
  private InvoiceService invoiceService;

  @PostMapping("/invoices")
  public ResponseEntity<InvoiceDto> create(@RequestBody InvoiceDto invoiceDto) {

    InvoiceEntity invoiceEntity = InvoiceEntity.builder()
            .number(invoiceDto.getNumber())
            .creationDate(invoiceDto.getCreationDate())
            .companyName(invoiceDto.getCompanyName())
            .paymentStatus(invoiceDto.getPaymentStatus())
            .totalCost(invoiceDto.getTotalCost())
            .build();
    InvoiceEntity createdInvoiceEntity = invoiceService.create(invoiceEntity);

    InvoiceDto createdInvoiceDto = InvoiceDto.builder()
            .number(createdInvoiceEntity.getNumber())
            .creationDate(createdInvoiceEntity.getCreationDate())
            .companyName(createdInvoiceEntity.getCompanyName())
            .paymentStatus(createdInvoiceEntity.getPaymentStatus())
            .totalCost(createdInvoiceEntity.getTotalCost())
            .build();

    return new ResponseEntity<InvoiceDto>(createdInvoiceDto, HttpStatus.CREATED);
  }
}
