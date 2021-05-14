package com.sms.invoicify.repository;

import com.sms.invoicify.models.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    InvoiceEntity findByNumber(long number);
}
