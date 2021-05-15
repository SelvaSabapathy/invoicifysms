package com.sms.invoicify.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyEntity {

  @Id
  @Column(nullable = false)
  String companyName;

  @Embedded Address address;
  String contactName;
  String title;
  String phoneNumber;
}
