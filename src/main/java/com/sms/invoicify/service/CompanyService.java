package com.sms.invoicify.service;

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

  public void createCompany(Company companyDto) {
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

    public void updateCompany(String companyName, Company company) {
    }

    public Company fetchCompanyByName(String companyName) {
    Optional<CompanyEntity> companyEntityOptional = companyRepository.findById(companyName);
    Company found = companyEntityOptional
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
