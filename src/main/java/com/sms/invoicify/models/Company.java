package com.sms.invoicify.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Company {

    String companyName;
    String address;
    String contactName;
    String title;
    int phoneNumber;

}
