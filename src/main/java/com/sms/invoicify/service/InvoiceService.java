package com.sms.invoicify.service;

import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class InvoiceService {

  @Autowired private InvoiceRepository invoiceRepository;

  public InvoiceEntity create(InvoiceEntity invoiceEntity) {
    BigDecimal invoiceTotalCost = invoiceEntity.getTotalCost() == null ? BigDecimal.valueOf(0).setScale(2) : invoiceEntity.getTotalCost();
    BigDecimal itemsTotalCost =
        invoiceEntity.getItems() == null
            ? new BigDecimal(0)
            : invoiceEntity.getItems().stream()
                .map(i -> i.getTotalFees())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    invoiceEntity.setTotalCost(invoiceTotalCost.add(itemsTotalCost));
    return invoiceRepository.save(invoiceEntity);
  }

  public List<InvoiceEntity> view() {
    return invoiceRepository.findAll();
  }

  public InvoiceEntity findByNumber(Long number) {
    return invoiceRepository.findByNumber(number);
  }
}
