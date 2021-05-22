package com.sms.invoicify.controller;

import com.sms.invoicify.exception.InvoicifyCompanyExistsException;
import com.sms.invoicify.models.Company;
import com.sms.invoicify.models.CompanySummaryVO;
import com.sms.invoicify.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
  public ResponseEntity<String> postCompanyDetails(@RequestBody Company company) {
    try {
      companyService.createCompany(company);
    } catch (InvoicifyCompanyExistsException e) {
      return new ResponseEntity<String>("Company exists, and can't be created", HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<String>(company.getCompanyName() + " created Successfully", HttpStatus.CREATED);
  }

  @PutMapping("/{companyName}")
  public ResponseEntity<String> updateCompany(@PathVariable("companyName") String companyName,
                                              @RequestBody Company company) {
    try {
      companyService.updateCompany(companyName, company);
    } catch (InvoicifyCompanyExistsException e) {
      return new ResponseEntity<String>("Company exists, and can't be created", HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<String>(companyName + " has been updated successfully.", HttpStatus.NO_CONTENT);
  }
}
