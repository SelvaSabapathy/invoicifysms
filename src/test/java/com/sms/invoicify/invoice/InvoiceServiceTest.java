package com.sms.invoicify.invoice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SpringBootTest
public class InvoiceServiceTest {

  @Mock private InvoiceRepository invoiceRepository;

  @InjectMocks private InvoiceService invoiceService;

  @AfterEach
  public void tearDown() {
    verifyNoMoreInteractions(invoiceRepository);
  }

  @Test
  public void create() {
    InvoiceEntity invoiceEntity = new InvoiceEntity();
    InvoiceEntity createdInvoiceEntity = invoiceService.create(invoiceEntity);

    verify(invoiceRepository).save(invoiceEntity);
  }
}
