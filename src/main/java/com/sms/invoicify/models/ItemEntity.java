package com.sms.invoicify.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long itemId;

  String description;
  Integer quantity;
  BigDecimal totalFees;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name="invoice_id")
  InvoiceEntity invoice;
}
