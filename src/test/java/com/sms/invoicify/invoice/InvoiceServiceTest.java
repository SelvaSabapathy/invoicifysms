package com.sms.invoicify.invoice;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
public class InvoiceServiceTest {

  @InjectMocks
  private InvoiceService invoiceService;

  @Test
  public void create() {
    InvoiceEntity invoiceEntity = new InvoiceEntity();
    InvoiceEntity createdInvoiceEntity = invoiceService.create(invoiceEntity);

    assertThat(createdInvoiceEntity, is(notNullValue()));
  }
}
