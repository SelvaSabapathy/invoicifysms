package com.sms.invoicify.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

  String street;
  String city;
  String state;
  String zipCode;
}
