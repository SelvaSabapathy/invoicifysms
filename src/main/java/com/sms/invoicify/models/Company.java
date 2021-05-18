package com.sms.invoicify.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

  @NotBlank(message = "Company Name is Required")
  String companyName;

  Address address;
  String contactName;
  String title;
  String phoneNumber;
}
