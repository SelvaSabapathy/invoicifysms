package com.sms.invoicify.repository;

import com.sms.invoicify.models.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepositiory extends JpaRepository<CompanyEntity, Long> {
}
