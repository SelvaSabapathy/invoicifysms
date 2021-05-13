package com.sms.invoicify;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CompanyIT {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void getCompanyWhenEmpty() throws Exception {
    mockMvc
        .perform(get("/company"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(0));
  }

  @Test
  void postCompanyDetails() throws Exception{
    Company company =  Company.builder().companyName("Test1").address("USA").contactName("Name1").title("Title1").phoneNumber(12345).build();
    mockMvc.perform(post("/company").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(company)))
            .andExpect(status().isCreated())
            .andExpect(content().string("Test1 created Successfully"))
            .andDo(document("PostCompanyDetails"));

    mockMvc
            .perform(get("/company"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("length()").value(1))
            .andExpect(jsonPath("[0].companyName").value("Test1"))
            .andExpect(jsonPath("[0].address").value("USA"))
            .andExpect(jsonPath("[0].contactName").value("Name1"))
            .andExpect(jsonPath("[0].title").value("Title1"))
            .andExpect(jsonPath("[0].phoneNumber").value(12345));

  }
}