package com.sms.invoicify.repository;

import com.sms.invoicify.models.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<CompanyEntity, String> {}
