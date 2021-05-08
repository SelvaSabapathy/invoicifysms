package com.sms.invoicify.invoice;

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
