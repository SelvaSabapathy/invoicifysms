package com.sms.invoicify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
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
  void postItem() throws Exception {
    Item item =
        Item.builder()
            .description("<LINE ITEM NAME>")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoice(
                InvoiceDto.builder()
                    .number(120L)
                    .creationDate(LocalDate.of(2021, 5, 17))
                    .lastModifiedDate(LocalDate.of(2021, 5, 17))
                    .companyName("SMS Corporation")
                    .paymentStatus(PaymentStatus.UNPAID)
                    .build())
            .build();
    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
        .andExpect(status().isCreated())
        .andExpect(content().string("<LINE ITEM NAME> created Successfully"))
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
                    fieldWithPath("invoice.number")
                        .description("Associated Invoice")
                        .attributes(key("constraints").value("Not Null")),
                    fieldWithPath("invoice.creationDate")
                        .description("Associated Invoice")
                        .attributes(key("constraints").value("Not Null"))
                        .ignored(),
                    fieldWithPath("invoice.lastModifiedDate")
                        .description("Associated Invoice")
                        .attributes(key("constraints").value("Not Null"))
                        .ignored(),
                    fieldWithPath("invoice.items")
                        .description("Associated Invoice")
                        .attributes(key("constraints").value("Not Null"))
                        .ignored(),
                    fieldWithPath("invoice.companyName")
                        .description("Associated Invoice")
                        .attributes(key("constraints").value("Not Null")),
                    paymentStatusField(),
                    fieldWithPath("invoice.totalCost")
                        .description("Associated Invoice")
                        .attributes(key("constraints").value("Integer, Not Null"))
                        .ignored()),
                responseBody()));

    mockMvc
        .perform(get("/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(1))
        .andExpect(jsonPath("[0].description").value("<LINE ITEM NAME>"))
        .andExpect(jsonPath("[0].quantity").value(1))
        .andExpect(jsonPath("[0].invoice.number").value(120))
        .andExpect(jsonPath("[0].totalFees").value(10.00))
    .andDo(document("{class-name}/{method-name}/{step}"));
  }

  private FieldDescriptor paymentStatusField() {
    String formattedValues =
        Arrays.stream(PaymentStatus.values())
            .map(type -> String.format("`%s`", type))
            .collect(Collectors.joining(", "));
    return fieldWithPath("invoice.paymentStatus")
        .description("The Current ENUM Payment status of the invoice.")
        .attributes(key("constraints").value("Not Null, One of: " + formattedValues));
  }

  @Test
  void postMultipleItem() throws Exception {
    Item item =
        Item.builder()
            .description("Widget Alpha")
            .quantity(1)
            .totalFees(BigDecimal.valueOf(29.99))
            .invoice(InvoiceDto.builder().number(120L).build())
            .build();
    Item item2 =
        Item.builder()
            .description("Sprocket Beta")
            .quantity(10)
            .totalFees(BigDecimal.TEN)
            .invoice(InvoiceDto.builder().number(120L).build())
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
        .andExpect(jsonPath("[0].invoice.number").value(120))
        .andExpect(jsonPath("[1].description").value("Sprocket Beta"))
        .andExpect(jsonPath("[1].quantity").value(10))
        .andExpect(jsonPath("[1].totalFees").value(10.00))
        .andExpect(jsonPath("[1].invoice.number").value(120))
        .andDo(
            document(
                "{class-name}/{method-name}/{step}",
                relaxedResponseFields(
                    fieldWithPath("[0].description")
                        .description("Test Item Description")
                        .attributes(key("info").value("hdksjhksjd")),
                    fieldWithPath("[0].quantity").description(1),
                    fieldWithPath("[0].totalFees").description(1.00),
                    fieldWithPath("[0].invoice.number").description("Invoice number"),
                    fieldWithPath("[0].invoice.creationDate").description("Invoice creation date"),
                    fieldWithPath("[0].invoice.lastModifiedDate")
                        .description("Invoice modified date"),
                    fieldWithPath("[0].invoice.items").description("Items in invoice"),
                    fieldWithPath("[0].invoice.companyName").description("Company Name"),
                    fieldWithPath("[0].invoice.paymentStatus")
                        .description("Invoice payment status"),
                    fieldWithPath("[0].invoice.totalCost").description("Invoice total cost"),
                    fieldWithPath("[1].description").description("Test Item Description2"),
                    fieldWithPath("[1].quantity").description(10),
                    fieldWithPath("[1].totalFees").description(10.00),
                    fieldWithPath("[1].invoice.number").description("Invoice number"),
                    fieldWithPath("[1].invoice.creationDate").description("Invoice creation date"),
                    fieldWithPath("[1].invoice.lastModifiedDate")
                        .description("Invoice modified date"),
                    fieldWithPath("[0].invoice.items").description("Items in invoice"),
                    fieldWithPath("[1].invoice.companyName").description("Company Name"),
                    fieldWithPath("[1].invoice.paymentStatus")
                        .description("Invoice payment status"),
                    fieldWithPath("[1].invoice.totalCost").description("Invoice total cost"))));
  }
}
