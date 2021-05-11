package com.sms.invoicify.service;

import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.repository.InvoiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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

  @Test
  public void fetchAll() {
    InvoiceEntity invoiceEntity1 = new InvoiceEntity();
    InvoiceEntity invoiceEntity2 = new InvoiceEntity();
    when(invoiceRepository.findAll()).thenReturn(List.of(invoiceEntity1, invoiceEntity2));

    List<InvoiceEntity> entityList = invoiceService.view();

    verify(invoiceRepository).findAll();
    assertThat(entityList, is(List.of(invoiceEntity1, invoiceEntity2)));
  }
}
