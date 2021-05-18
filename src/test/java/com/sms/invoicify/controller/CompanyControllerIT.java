package com.sms.invoicify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.invoicify.models.Address;
import com.sms.invoicify.models.Company;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CompanyControllerIT {

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  @Test
  void getCompanyWhenEmpty() throws Exception {
    mockMvc
        .perform(get("/company"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(0));
  }

  @Test
  void postCompanyDetails() throws Exception {
    Company company =
        Company.builder()
            .companyName("Hampton DeVille Corp.")
            .address(
                Address.builder()
                    .street("100 N State Street")
                    .city("Chicago")
                    .state("IL")
                    .zipCode("60601")
                    .build())
            .contactName("Jane Smith")
            .title("VP - Accounts")
            .phoneNumber("312-777-7777")
            .build();
    mockMvc
        .perform(
            post("/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(company)))
        .andExpect(status().isCreated())
        .andExpect(content().string("Hampton DeVille Corp. created Successfully"))
        .andDo(document("PostCompanyDetails"));

    mockMvc
        .perform(get("/company"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(1))
        .andExpect(jsonPath("[0].companyName").value("Hampton DeVille Corp."))
        .andExpect(jsonPath("[0].address.street").value("100 N State Street"))
        .andExpect(jsonPath("[0].address.city").value("Chicago"))
        .andExpect(jsonPath("[0].address.state").value("IL"))
        .andExpect(jsonPath("[0].address.zipCode").value("60601"))
        .andExpect(jsonPath("[0].contactName").value("Jane Smith"))
        .andExpect(jsonPath("[0].title").value("VP - Accounts"))
        .andExpect(jsonPath("[0].phoneNumber").value("312-777-7777"))
        .andDo(
            document(
                "GetAllCompanies",
                responseFields(
                    fieldWithPath("[0].companyName").description("Company Name"),
                    fieldWithPath("[0].address.street").description("Street Address"),
                    fieldWithPath("[0].address.city").description("Location City"),
                    fieldWithPath("[0].address.state").description("Location State"),
                    fieldWithPath("[0].address.zipCode").description("Postal Zip Code"),
                    fieldWithPath("[0].contactName").description("Primary Account Contact Name"),
                    fieldWithPath("[0].title")
                        .description("Primary Account Contact Position Title"),
                    fieldWithPath("[0].phoneNumber").description("Phone Number"))));
  }
}
