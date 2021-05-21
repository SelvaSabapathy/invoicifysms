package com.sms.invoicify.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
  String description;
  Integer quantity;

  @NumberFormat(style = NumberFormat.Style.CURRENCY)
  BigDecimal totalFees;

  Long invoiceNumber;
}
