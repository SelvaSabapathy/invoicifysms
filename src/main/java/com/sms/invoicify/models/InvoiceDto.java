package com.sms.invoicify.models;

import com.sms.invoicify.utilities.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDto {
    private long number;
    private Date creationDate;
    private Date lastModifiedDate;
//    private List<Item> items; @TODO: add after Item is added to project
    private String companyName;
    private PaymentStatus paymentStatus;
    private double totalCost;
}
