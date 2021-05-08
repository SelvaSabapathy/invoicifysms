package com.sms.invoicify.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSummaryDto {
    private long number;
    private Date creationDate;
    private PaymentStatus paymentStatus;
    private double totalCost;
}
