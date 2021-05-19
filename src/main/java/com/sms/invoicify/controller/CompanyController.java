package com.sms.invoicify.controller;

import com.sms.invoicify.models.Company;
import com.sms.invoicify.models.CompanySummaryVO;
import com.sms.invoicify.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

  CompanyService companyService;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<Company> getCompanyDetails() {
    return companyService.fetchAllCompany();
  }

  @GetMapping("/summary")
  @ResponseStatus(HttpStatus.OK)
  public List<CompanySummaryVO> getCompanySummary() {
    return companyService.fetchCompanySummaryView();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public String postCompanyDetails(@RequestBody Company company) {
    companyService.createCompany(company);
    return company.getCompanyName() + " created Successfully";
  }
}
