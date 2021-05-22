package com.sms.invoicify.repository;

import com.sms.invoicify.models.InvoiceEntity;
import com.sms.invoicify.utilities.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
  InvoiceEntity findByNumber(long number);

  List<InvoiceEntity> findByPaymentStatus(PaymentStatus paymentStatus);

  List<InvoiceEntity> findByCompanyNameAndPaymentStatusOrderByCreationDateAsc(String companyName, PaymentStatus paymentStatus);

  @Query(
      value =
          "SELECT entity FROM InvoiceEntity entity WHERE entity.creationDate < :oneYearAgo AND entity.paymentStatus = :paymentStatus")
  List<InvoiceEntity> findYearOldandPaid(
      @Param("oneYearAgo") LocalDate oneYearAgo,
      @Param("paymentStatus") PaymentStatus paymentStatus);
}
