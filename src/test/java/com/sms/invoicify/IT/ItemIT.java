package com.sms.invoicify.IT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.invoicify.models.InvoiceDto;
import com.sms.invoicify.models.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

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
public class ItemIT {
  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  @Test
  void getItemsWhenEmpty() throws Exception {
    mockMvc
        .perform(get("/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(0));
  }

  @Test
  void postItem() throws Exception {
    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoice(InvoiceDto.builder().number(120).build())
            .build();
    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
        .andExpect(status().isCreated())
        .andExpect(content().string("Test Item Description created Successfully"))
        .andDo(document("PostNewItem"));

    mockMvc
        .perform(get("/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(1))
        .andExpect(jsonPath("[0].description").value("Test Item Description"))
        .andExpect(jsonPath("[0].quantity").value(1))
        .andExpect(jsonPath("[0].invoice.number").value(120))
        .andExpect(jsonPath("[0].totalFees").value(10.00));
  }

  @Test
  void postMultipleItem() throws Exception {
    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoice(InvoiceDto.builder().number(120).build())
            .build();
    Item item1 =
        Item.builder()
            .description("Test Item Description2")
            .quantity(2)
            .totalFees(BigDecimal.valueOf(5))
            .invoice(InvoiceDto.builder().number(121).build())
            .build();
    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
        .andExpect(status().isCreated())
        .andExpect(content().string("Test Item Description created Successfully"));

    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item1)))
        .andExpect(status().isCreated())
        .andExpect(content().string("Test Item Description2 created Successfully"));

    mockMvc
        .perform(get("/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(2))
        .andExpect(jsonPath("[0].description").value("Test Item Description"))
        .andExpect(jsonPath("[0].quantity").value(1))
        .andExpect(jsonPath("[0].totalFees").value(10.00))
        .andExpect(jsonPath("[0].invoice.number").value(120))
        .andExpect(jsonPath("[1].description").value("Test Item Description2"))
        .andExpect(jsonPath("[1].quantity").value(2))
        .andExpect(jsonPath("[1].totalFees").value(5.00))
        .andExpect(jsonPath("[1].invoice.number").value(121))
        .andDo(
            document(
                "GetAllItems",
                responseFields(
                    fieldWithPath("[0].description").description("Test Item Description"),
                    fieldWithPath("[0].quantity").description(1),
                    fieldWithPath("[0].totalFees").description(10.00),
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
                    fieldWithPath("[1].quantity").description(2),
                    fieldWithPath("[1].totalFees").description(5),
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
