package com.sms.invoicify.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private long number;

    private Date creationDate;

    private Date lastModifiedDate;

    //    private List<Item> items; @TODO: add after Item is added to project

    private String companyName;

    @Enumerated(EnumType.ORDINAL)
    private PaymentStatus paymentStatus;

    private double totalCost;
}
