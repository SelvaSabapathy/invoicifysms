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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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


    @Test
    @DisplayName("Get All Company")
    void getAllCompany() {
        when(companyRepositiory.findAll())
                .thenReturn(
                        List.of(
                                CompanyEntity.builder()
                                        .companyId(8L)
                                        .companyName("Test1").address("USA").contactName("Name1").title("Title1").phoneNumber(12345).build()));

        List<Company> companyFromService = companyService.fetchAllCompany();

        Company companyDtoExpected =
                Company.builder()
                        .companyName("Test1").address("USA").contactName("Name1").title("Title1").phoneNumber(12345).build();

        assertThat(companyFromService).isEqualTo(List.of(companyDtoExpected));

        verify(companyRepositiory).findAll();
        verifyNoMoreInteractions(companyRepositiory);
    }
}
