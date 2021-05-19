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
  void getCompanySummaryView() throws Exception {
    Company company1 =
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
    Company company2 =
            Company.builder()
                    .companyName("Kitchener Corp.")
                    .address(
                            Address.builder()
                                    .street("100 N State Street")
                                    .city("Kitchener")
                                    .state("ON")
                                    .zipCode("N2G 1B6")
                                    .build())
                    .contactName("Ravi Chellappa")
                    .title("Tech Product Owner")
                    .phoneNumber("215-366-7320")
                    .build();
    mockMvc
            .perform(
                    post("/company")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(company1)))
            .andExpect(status().isCreated())
            .andExpect(content().string("Hampton DeVille Corp. created Successfully"));
    mockMvc
            .perform(
                    post("/company")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(company2)))
            .andExpect(status().isCreated())
            .andExpect(content().string("Kitchener Corp. created Successfully"));
    mockMvc
            .perform(get("/company/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("length()").value(2))
            .andExpect(jsonPath("[0].companyName").value("Hampton DeVille Corp."))
            .andExpect(jsonPath("[0].city").value("Chicago"))
            .andExpect(jsonPath("[0].state").value("IL"))
            .andExpect(jsonPath("[1].companyName").value("Kitchener Corp."))
            .andExpect(jsonPath("[1].city").value("Kitchener"))
            .andExpect(jsonPath("[1].state").value("ON"))
            .andDo(
                    document(
                            "{class-name}/{method-name}/{step}",
                            responseFields(
                                    fieldWithPath("[0].companyName").description("Company Name"),
                                    fieldWithPath("[0].city").description("Location City"),
                                    fieldWithPath("[0].state").description("Location State"),
                                    fieldWithPath("[1].companyName").description("Company Name"),
                                    fieldWithPath("[1].city").description("Location City"),
                                    fieldWithPath("[1].state").description("Location State")),
                    responseBody()));

    }
}