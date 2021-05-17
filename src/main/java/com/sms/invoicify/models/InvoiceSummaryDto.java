package com.sms.invoicify.models;

import com.sms.invoicify.utilities.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSummaryDto {
    private Long number;
    private LocalDate creationDate;
    private PaymentStatus paymentStatus;
    private Double totalCost;
}
