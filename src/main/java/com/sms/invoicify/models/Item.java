package com.sms.invoicify.models;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Item {
    String description;
    int quantity;
    BigDecimal totalFees;

}
