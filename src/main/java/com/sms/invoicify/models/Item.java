package com.sms.invoicify.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
  String description;
  int quantity;
  BigDecimal totalFees;
  InvoiceDto invoice;
}
