package com.sms.invoicify.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long companyId;

  String companyName;
  String address;
  String contactName;
  String title;
  int phoneNumber;
}
