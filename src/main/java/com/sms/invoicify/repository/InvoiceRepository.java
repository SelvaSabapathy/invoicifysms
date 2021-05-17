package com.sms.invoicify.repository;

import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.utilities.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    InvoiceEntity findByNumber(long number);

    @Query(value = "DELETE FROM InvoiceEntity i WHERE i.creationDate > :oneYearAgo AND i.paymentStatus = :paymentStatus")
    Object deleteYearOldAndPaid(Date oneYearAgo, PaymentStatus paymentStatus);
}
