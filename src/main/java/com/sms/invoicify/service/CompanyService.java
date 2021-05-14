package com.sms.invoicify.service;

import com.sms.invoicify.models.Company;
import com.sms.invoicify.models.CompanyEntity;
import com.sms.invoicify.repository.CompanyRepositiory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {
    private final CompanyRepositiory companyRepositiory;

    public void createCompany(Company companyDto) {
        companyRepositiory.save(CompanyEntity.builder()
                .companyName(companyDto.getCompanyName())
                .address(companyDto.getAddress())
                .contactName(companyDto.getContactName())
                .title(companyDto.getTitle())
                .phoneNumber(companyDto.getPhoneNumber())
                .build());
    }

    public List<Company> fetchAllCompany() {
        return companyRepositiory.findAll().stream()
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
}
