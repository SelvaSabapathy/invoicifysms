package com.sms.invoicify.service;

import com.sms.invoicify.exception.InvoicifyInvoiceExistsException;
import com.sms.invoicify.exception.InvoicifyInvoiceNotExistsException;
import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.repository.InvoiceRepository;
import com.sms.invoicify.utilities.PaymentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
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
  public void create() throws InvoicifyInvoiceExistsException {
    InvoiceEntity invoiceEntity = new InvoiceEntity();
    invoiceEntity.setNumber(100L);
    invoiceService.create(invoiceEntity);

    verify(invoiceRepository).findByNumber(100L);
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

  @Test
  public void findByNumberTest() {
    InvoiceEntity invoiceEntity = new InvoiceEntity();
    when(invoiceRepository.findByNumber(8L)).thenReturn(invoiceEntity);

    InvoiceEntity actual = invoiceService.findByNumber(8L);
    verify(invoiceRepository).findByNumber(8L);
    assertThat(actual, is(invoiceEntity));
  }

  @Test
  public void updateInvoice()
      throws InvoicifyInvoiceExistsException, InvoicifyInvoiceNotExistsException, ParseException {
    InvoiceEntity invoiceEntity = new InvoiceEntity();
    invoiceEntity.setNumber(100L);
    when(invoiceRepository.findByNumber(100L)).thenReturn(invoiceEntity);

    // update
    invoiceEntity.setPaymentStatus(PaymentStatus.PAID);
    invoiceService.update(invoiceEntity);
    verify(invoiceRepository).findByNumber(100L);
    verify(invoiceRepository).save(invoiceEntity);
  }
}
