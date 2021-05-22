package com.sms.invoicify.service;

import com.sms.invoicify.exception.InvoicifyCompanyExistsException;
import com.sms.invoicify.exception.InvoicifyCompanyNotExistsException;
import com.sms.invoicify.models.Address;
import com.sms.invoicify.models.Company;
import com.sms.invoicify.models.CompanyEntity;
import com.sms.invoicify.models.CompanySummaryVO;
import com.sms.invoicify.repository.CompanyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {
  private final CompanyRepository companyRepository;

  private void saveCompany(Company companyDto) {
    companyRepository.save(
            CompanyEntity.builder()
                    .companyName(companyDto.getCompanyName())
                    .address(
                            Address.builder()
                                    .street(companyDto.getAddress().getStreet())
                                    .city(companyDto.getAddress().getCity())
                                    .state(companyDto.getAddress().getState())
                                    .zipCode(companyDto.getAddress().getZipCode())
                                    .build())
                    .contactName(companyDto.getContactName())
                    .title(companyDto.getTitle())
                    .phoneNumber(companyDto.getPhoneNumber())
                    .build());
  }

  public void createCompany(Company companyDto) throws InvoicifyCompanyExistsException {
    if (fetchCompanyByName(companyDto.getCompanyName()) != null) {
      throw new InvoicifyCompanyExistsException("Company exists. Can't be created");
    }
    saveCompany(companyDto);
  }

  public List<Company> fetchAllCompany() {
    return companyRepository.findAll().stream()
        .map(
            companyEntity -> {
              return Company.builder()
                  .companyName(companyEntity.getCompanyName())
                  .address(companyEntity.getAddress())
                  .contactName(companyEntity.getContactName())
                  .title(companyEntity.getTitle())
                  .phoneNumber(companyEntity.getPhoneNumber())
                  .build();
            })
        .collect(Collectors.toList());
  }

  public List<CompanySummaryVO> fetchCompanySummaryView() {
    return companyRepository.findAll().stream()
        .map(
            companyEntity -> {
              return CompanySummaryVO.builder()
                  .companyName(companyEntity.getCompanyName())
                  .city(companyEntity.getAddress().getCity())
                  .state(companyEntity.getAddress().getState())
                  .build();
            })
        .collect(Collectors.toList());
  }

  public void updateCompany(String companyName, Company company) throws InvoicifyCompanyExistsException {
    if (company.getCompanyName() != null
        && companyName.equalsIgnoreCase(company.getCompanyName())) {
      Company existingCompany = this.fetchCompanyByName(companyName);

      Optional.ofNullable(company.getAddress().getStreet()).ifPresent(existingCompany.getAddress()::setStreet);
      Optional.ofNullable(company.getAddress().getCity()).ifPresent(existingCompany.getAddress()::setCity);
      Optional.ofNullable(company.getAddress().getState()).ifPresent(existingCompany.getAddress()::setState);
      Optional.ofNullable(company.getAddress().getZipCode()).ifPresent(existingCompany.getAddress()::setZipCode);
      Optional.ofNullable(company.getContactName()).ifPresent(existingCompany::setContactName);
      Optional.ofNullable(company.getTitle()).ifPresent(existingCompany::setTitle);
      Optional.ofNullable(company.getPhoneNumber()).ifPresent(existingCompany::setPhoneNumber);

      saveCompany(existingCompany);
    }
  }

  public Company fetchCompanyByName(String companyName) {
    Optional<CompanyEntity> companyEntityOptional = companyRepository.findById(companyName);
    Company found =
        companyEntityOptional
            .map(
                companyEntity -> {
                  return Company.builder()
                      .companyName(companyEntity.getCompanyName())
                      .address(companyEntity.getAddress())
                      .contactName(companyEntity.getContactName())
                      .title(companyEntity.getTitle())
                      .phoneNumber(companyEntity.getPhoneNumber())
                      .build();
                })
            .orElse(null);
    return found;
  }
}
