package com.sms.invoicify.service;

import com.sms.invoicify.models.Company;
import com.sms.invoicify.models.CompanyEntity;
import com.sms.invoicify.repository.CompanyRepositiory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {

    @Mock
    CompanyRepositiory companyRepositiory;

    @InjectMocks
    CompanyService companyService;

    @Test
    @DisplayName("Create new Company Test")
    void createCompany() {
        Company companyDto = Company.builder()
                .companyName("Test1").address("USA").contactName("Name1").title("Title1").phoneNumber(12345).build();

        companyService.createCompany(companyDto);

        verify(companyRepositiory).save(
                CompanyEntity.builder()
                        .companyName("Test1").address("USA").contactName("Name1").title("Title1").phoneNumber(12345).build()
        );
        verifyNoMoreInteractions(companyRepositiory);
    }


}
