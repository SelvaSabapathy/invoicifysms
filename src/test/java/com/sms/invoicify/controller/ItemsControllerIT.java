package com.sms.invoicify.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.invoicify.models.Address;
import com.sms.invoicify.models.Company;
import com.sms.invoicify.models.InvoiceDto;
import com.sms.invoicify.models.Item;
import com.sms.invoicify.utilities.PaymentStatus;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseBody;
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
public class ItemsControllerIT {

  @Autowired ObjectMapper objectMapper;

  private MockMvc mockMvc;

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
  void getItemsWhenEmpty() throws Exception {
    mockMvc
        .perform(get("/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(0))
        .andDo(document("{class-name}/{method-name}/{step}"));
  }

  @Test
  void postItemToInvoiceWhichDoesNotExist() throws Exception {
    Item item =
        Item.builder()
            .description("INVOICE_ITEM_NAME")
            .quantity(1)
            .totalFees(BigDecimal.valueOf(10).setScale(2))
            .invoiceNumber(120L)
            .build();
    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invoice does not exist. Please use valid invoice."))
        .andDo(document("{class-name}/{method-name}/{step}"));
  }

  @Test
  void postItem() throws Exception {
    Company company =
            Company.builder()
                    .companyName("aCompany")
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
            .andExpect(content().string("aCompany created Successfully"));

    Item item =
        Item.builder()
            .description("INVOICE_ITEM_NAME")
            .quantity(1)
            .totalFees(BigDecimal.valueOf(10).setScale(2))
            .invoiceNumber(120L)
            .build();

    // post invoice 120
    InvoiceDto invoiceDto =
        new InvoiceDto(
            120L,
            LocalDate.now(),
            null,
            null,
            "aCompany",
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));

    mockMvc
        .perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoiceDto)))
        .andExpect(status().isCreated());

    // post items
    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
        .andExpect(status().isCreated())
        .andExpect(content().string("INVOICE_ITEM_NAME created Successfully"))
        .andDo(
            document(
                "{class-name}/{method-name}/{step}",
                relaxedRequestFields(
                    attributes(key("title").value("Fields for Item Creation")),
                    fieldWithPath("description")
                        .description("Item Description")
                        .attributes(key("constraints").value("Not Null")),
                    fieldWithPath("quantity")
                        .description("Item Quantity")
                        .attributes(key("constraints").value("Not Null")),
                    fieldWithPath("totalFees")
                        .description("Total charges in $USD")
                        .attributes(key("constraints").value("Not Null")),
                    fieldWithPath("invoiceNumber")
                        .description("Associated Invoice")
                        .attributes(key("constraints").value("Not Null"))),
                responseBody()));

    mockMvc
        .perform(get("/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(1))
        .andExpect(jsonPath("[0].description").value("INVOICE_ITEM_NAME"))
        .andExpect(jsonPath("[0].quantity").value(1))
        .andExpect(jsonPath("[0].invoiceNumber").value(120))
        .andExpect(jsonPath("[0].totalFees").value(10.00))
        .andDo(document("{class-name}/{method-name}/{step}"));

    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices/search/120").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    List<InvoiceDto> invoiceDtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<InvoiceDto>>() {});
    assertThat(invoiceDtos.size(), is(1));
    assertThat(invoiceDtos.get(0).getNumber(), is(120L));
    assertThat(invoiceDtos.get(0).getTotalCost(), is(BigDecimal.valueOf(130).setScale(2)));
  }

  @Test
  void postMultipleItem() throws Exception {

    Company company =
            Company.builder()
                    .companyName("aCompany")
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
            .andExpect(content().string("aCompany created Successfully"));

    // post invoice 120
    InvoiceDto invoiceDto =
        new InvoiceDto(
            120L,
            LocalDate.now(),
            null,
            null,
            "aCompany",
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));

    mockMvc
        .perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoiceDto)))
        .andExpect(status().isCreated());

    Item item =
        Item.builder()
            .description("Widget Alpha")
            .quantity(1)
            .totalFees(BigDecimal.valueOf(29.99))
            .invoiceNumber(120L)
            .build();
    Item item2 =
        Item.builder()
            .description("Sprocket Beta")
            .quantity(10)
            .totalFees(BigDecimal.TEN)
            .invoiceNumber(120L)
            .build();
    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
        .andExpect(status().isCreated())
        .andExpect(content().string("Widget Alpha created Successfully"));

    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item2)))
        .andExpect(status().isCreated())
        .andExpect(content().string("Sprocket Beta created Successfully"));

    mockMvc
        .perform(get("/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(2))
        .andExpect(jsonPath("[0].description").value("Widget Alpha"))
        .andExpect(jsonPath("[0].quantity").value(1))
        .andExpect(jsonPath("[0].totalFees").value(29.99))
        .andExpect(jsonPath("[0].invoiceNumber").value(120))
        .andExpect(jsonPath("[1].description").value("Sprocket Beta"))
        .andExpect(jsonPath("[1].quantity").value(10))
        .andExpect(jsonPath("[1].totalFees").value(10.00))
        .andExpect(jsonPath("[1].invoiceNumber").value(120))
        .andDo(
            document(
                "{class-name}/{method-name}/{step}",
                relaxedResponseFields(
                    fieldWithPath("[0].description")
                        .description("Test Item Description")
                        .attributes(key("info").value("hdksjhksjd")),
                    fieldWithPath("[0].quantity").description(1),
                    fieldWithPath("[0].totalFees").description(1.00),
                    fieldWithPath("[0].invoiceNumber").description("Invoice number"),
                    fieldWithPath("[1].description").description("Test Item Description2"),
                    fieldWithPath("[1].quantity").description(10),
                    fieldWithPath("[1].totalFees").description(10.00),
                    fieldWithPath("[1].invoiceNumber").description("Invoice number"))));

    // validate invoice details after adding items
    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices/search/120").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    List<InvoiceDto> invoiceDtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<InvoiceDto>>() {});
    assertThat(invoiceDtos.size(), is(1));
    assertThat(invoiceDtos.get(0).getNumber(), is(120L));
    assertThat(invoiceDtos.get(0).getTotalCost(), is(BigDecimal.valueOf(159.99).setScale(2)));
  }
}
