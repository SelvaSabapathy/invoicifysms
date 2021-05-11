package com.sms.invoicify.service;

import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    public InvoiceEntity create(InvoiceEntity invoiceEntity) {
        return invoiceRepository.save(invoiceEntity);
    }

    public List<InvoiceEntity> view() {
        return invoiceRepository.findAll();
    }
}
