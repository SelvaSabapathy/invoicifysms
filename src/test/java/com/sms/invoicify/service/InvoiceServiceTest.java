package com.sms.invoicify.service;

import com.sms.invoicify.exception.InvoicifyCompanyNotExistsException;
import com.sms.invoicify.exception.InvoicifyInvoiceExistsException;
import com.sms.invoicify.exception.InvoicifyInvoiceNotExistsException;
import com.sms.invoicify.models.Company;
import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.repository.InvoiceRepository;
import com.sms.invoicify.utilities.PaymentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
public class InvoiceServiceTest {

  @Mock private InvoiceRepository invoiceRepository;

  @InjectMocks private InvoiceService invoiceService;

  @AfterEach
  public void tearDown() {
    verifyNoMoreInteractions(invoiceRepository, companyService);
  }

  @Mock CompanyService companyService;

  @Test
  public void create() throws InvoicifyInvoiceExistsException, InvoicifyCompanyNotExistsException {
    InvoiceEntity invoiceEntity = new InvoiceEntity();
    invoiceEntity.setNumber(100L);
    invoiceEntity.setCompanyName("aCompany");
    when(companyService.fetchCompanyByName(anyString()))
        .thenReturn(Company.builder().companyName("aCompany").build());
    invoiceService.create(invoiceEntity);

    assertEquals("aCompany", invoiceEntity.getCompanyName());
    verify(companyService).fetchCompanyByName("aCompany");
    verify(invoiceRepository).findByNumber(100L);
    verify(invoiceRepository).save(invoiceEntity);
  }

  @Test
  public void fetchAll() {
    InvoiceEntity invoiceEntity1 = new InvoiceEntity();
    InvoiceEntity invoiceEntity2 = new InvoiceEntity();
    when(invoiceRepository.findByOrderByCreationDateAsc())
        .thenReturn(List.of(invoiceEntity1, invoiceEntity2));

    List<InvoiceEntity> entityList = invoiceService.viewAllinvoices();

    verify(invoiceRepository).findByOrderByCreationDateAsc();
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
  public void updateInvoice() throws InvoicifyInvoiceNotExistsException, ParseException {
    InvoiceEntity invoiceEntity = new InvoiceEntity();
    invoiceEntity.setNumber(100L);
    when(invoiceRepository.findByNumber(100L)).thenReturn(invoiceEntity);

    // update
    invoiceEntity.setPaymentStatus(PaymentStatus.PAID);
    invoiceService.update(invoiceEntity);
    verify(invoiceRepository).findByNumber(100L);
    verify(invoiceRepository).save(invoiceEntity);
  }

  @Test
  public void deleteInvoices() throws ParseException {
    LocalDate oneYearAgo = LocalDate.now().minusYears(1L);
    PaymentStatus paymentStatus = PaymentStatus.PAID;

    InvoiceEntity invoiceEntity = new InvoiceEntity();
    invoiceEntity.setNumber(100L);
    when(invoiceRepository.findYearOldandPaid(oneYearAgo, paymentStatus))
        .thenReturn(List.of(invoiceEntity));

    invoiceService.delete();
    verify(invoiceRepository).findYearOldandPaid(oneYearAgo, paymentStatus);
    verify(invoiceRepository).delete(invoiceEntity);
  }

  @Test
  public void findUnpaidInvoiceTest() {
    InvoiceEntity invoiceEntity = new InvoiceEntity();
    when(invoiceRepository.findByCompanyNameAndPaymentStatus(
            anyString(), eq(PaymentStatus.UNPAID), any()))
        .thenReturn(List.of(invoiceEntity));

    List<InvoiceEntity> actual =
        invoiceService.findByCompanyNameAndPaymentStatus("aCompany", PaymentStatus.UNPAID, 0, 2);
    verify(invoiceRepository)
        .findByCompanyNameAndPaymentStatus(anyString(), eq(PaymentStatus.UNPAID), any());
    assertThat(actual, is(List.of(invoiceEntity)));
  }

  @Test
  void create_throwsException_whenCompanyDoesNotExist()
      throws InvoicifyInvoiceExistsException, InvoicifyCompanyNotExistsException {
    InvoiceEntity invoiceEntity = new InvoiceEntity();
    invoiceEntity.setNumber(100L);
    invoiceEntity.setCompanyName("aCompany");

    when(invoiceRepository.findByNumber(anyLong())).thenReturn(null);
    when(companyService.fetchCompanyByName(anyString())).thenReturn(null);

    assertThrows(
        InvoicifyCompanyNotExistsException.class,
        () -> {
          invoiceService.create(invoiceEntity);
        });

    verify(companyService).fetchCompanyByName("aCompany");
    verify(invoiceRepository).findByNumber(100L);
  }

  @Test
  void create_throwsException_whenPreviousInvoiceNumberExists()
      throws InvoicifyInvoiceExistsException, InvoicifyCompanyNotExistsException {
    InvoiceEntity invoiceEntity = new InvoiceEntity();
    invoiceEntity.setNumber(100L);
    invoiceEntity.setCompanyName("aCompany");

    when(invoiceRepository.findByNumber(anyLong())).thenReturn(InvoiceEntity.builder().build());

    assertThrows(
        InvoicifyInvoiceExistsException.class,
        () -> {
          invoiceService.create(invoiceEntity);
        });

    verify(invoiceRepository).findByNumber(100L);
  }
}
