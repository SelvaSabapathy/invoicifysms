package com.sms.invoicify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.invoicify.models.Address;
import com.sms.invoicify.models.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseBody;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CompanyControllerIT {

  private MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  @BeforeEach
  public void setUp(
      WebApplicationContext webApplicationContext,
      RestDocumentationContextProvider restDocumentation) {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(prettyPrint())
                    .withResponseDefaults(prettyPrint()))
            .alwaysDo(document("{class-name}/{method-name}/{step}"))
            .build();
  }

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
        .andDo(
            document(
                "{class-name}/{method-name}/{step}",
                relaxedRequestFields(
                    attributes(key("title").value("Fields for adding new Company")),
                    fieldWithPath("companyName")
                        .description("Name of the Company")
                        .attributes(key("constraints").value("Not Blank, Primary Key")),
                    fieldWithPath("address.street")
                        .description("Street Address of Company")
                        .attributes(key("constraints").value("Not Null")),
                    fieldWithPath("address.city")
                        .description("Location City")
                        .attributes(key("constraints").value("Not Null")),
                    fieldWithPath("address.state")
                        .description("Location State")
                        .attributes(key("constraints").value("Not Null")),
                    fieldWithPath("address.zipCode")
                        .description("US Postal ZipCode")
                        .attributes(key("constraints").value("Not Null, 5 Digits")),
                    fieldWithPath("contactName")
                        .description("Name of Primary Contact")
                        .attributes(key("constraints").value("")),
                    fieldWithPath("title")
                        .description("Title of Primary Contact")
                        .attributes(key("constraints").value("")),
                    fieldWithPath("phoneNumber")
                        .description("Phone Number of Primary Contact")
                        .attributes(key("constraints").value(""))),
                responseBody()));

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
                "{class-name}/{method-name}/{step}",
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

  @Test
  void updateCompany_whenCompanyExists() throws Exception {
    Company updatedCompany =
        Company.builder()
            .companyName("Hampton DeVille Corp.")
            .address(
                Address.builder()
                    .street("200 W Lake Street")
                    .city("Chicago")
                    .state("IL")
                    .zipCode("60602")
                    .build())
            .contactName("Mary Jones")
            .title("President - Accounts")
            .phoneNumber("312-777-8888")
            .build();

    this.postCompanyDetails();

    mockMvc
        .perform(
            put("/company/{companyName}", updatedCompany.getCompanyName())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCompany)))
        .andExpect(status().isNoContent())
            .andDo(
                    document(
                            "{class-name}/{method-name}/{step}",
                            relaxedRequestFields(
                                    attributes(key("title").value("Fields for adding new Company")),
                                    fieldWithPath("companyName")
                                            .description("Name of the Company")
                                            .attributes(key("constraints").value("Not Blank, Primary Key")),
                                    fieldWithPath("address.street")
                                            .description("Street Address of Company")
                                            .attributes(key("constraints").value("Not Null")),
                                    fieldWithPath("address.city")
                                            .description("Location City")
                                            .attributes(key("constraints").value("Not Null")),
                                    fieldWithPath("address.state")
                                            .description("Location State")
                                            .attributes(key("constraints").value("Not Null")),
                                    fieldWithPath("address.zipCode")
                                            .description("US Postal ZipCode")
                                            .attributes(key("constraints").value("Not Null, 5 Digits")),
                                    fieldWithPath("contactName")
                                            .description("Name of Primary Contact")
                                            .attributes(key("constraints").value("")),
                                    fieldWithPath("title")
                                            .description("Title of Primary Contact")
                                            .attributes(key("constraints").value("")),
                                    fieldWithPath("phoneNumber")
                                            .description("Phone Number of Primary Contact")
                                            .attributes(key("constraints").value(""))),
                            responseBody()));

    mockMvc.perform(get("/company"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("length()").value(1))
            .andExpect(jsonPath("[0].companyName").value("Hampton DeVille Corp."))
            .andExpect(jsonPath("[0].address.street").value("200 W Lake Street"))
            .andExpect(jsonPath("[0].address.city").value("Chicago"))
            .andExpect(jsonPath("[0].address.state").value("IL"))
            .andExpect(jsonPath("[0].address.zipCode").value("60602"))
            .andExpect(jsonPath("[0].contactName").value("Mary Jones"))
            .andExpect(jsonPath("[0].title").value("President - Accounts"))
            .andExpect(jsonPath("[0].phoneNumber").value("12-777-8888"));

  }
}
