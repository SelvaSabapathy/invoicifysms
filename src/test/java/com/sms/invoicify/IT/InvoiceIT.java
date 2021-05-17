package com.sms.invoicify.IT;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.invoicify.models.InvoiceDto;
import com.sms.invoicify.models.InvoiceSummaryDto;
import com.sms.invoicify.models.Item;
import com.sms.invoicify.utilities.InvoicifyUtilities;
import com.sms.invoicify.utilities.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class InvoiceIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private MvcResult create(InvoiceDto invoiceDto, HttpStatus status) throws Exception {
    MvcResult mvcResult =
        this.createInner(invoiceDto, status).andDo(document("createNewInvoice")).andReturn();

    return mvcResult;
  }

  private ResultActions createInner(InvoiceDto invoiceDto, HttpStatus status) throws Exception {
    return mockMvc
        .perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoiceDto)))
        .andExpect(status().is(status.value()));
  }

  private ResultActions update(InvoiceDto invoiceDto, HttpStatus status) throws Exception {
    return mockMvc
        .perform(
            put("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoiceDto)))
        .andExpect(status().is(status.value()));
  }

  @Test
  public void createOneFailureNullNumber() throws Exception {
    InvoiceDto invoiceDto =
        new InvoiceDto(
            null,
            InvoicifyUtilities.getDate(LocalDate.now()),
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
        .andExpect(status().isBadRequest())
        .andDo(document("postInvoiceWithNoInvoiceNumber"))
        .andReturn();
  }

  @Test
  public void createOneFailureExistingNumber() throws Exception {
    InvoiceDto invoiceDto =
        new InvoiceDto(
            121L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            null,
            "aCompany",
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    MvcResult mvcResult = create(invoiceDto, HttpStatus.CREATED);

    mockMvc
        .perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoiceDto)))
        .andExpect(status().isBadRequest())
        .andDo(document("postInvoiceWithExistingInvoiceNumber"))
        .andReturn();
  }

  @Test
  public void createOneSuccess() throws Exception {
    InvoiceDto invoiceDto =
        new InvoiceDto(
            121L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            null,
            "aCompany",
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    MvcResult mvcResult = create(invoiceDto, HttpStatus.CREATED);

    InvoiceDto createdInvoiceCto =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), InvoiceDto.class);

    assertThat(createdInvoiceCto, is(invoiceDto));
  }

  @Test
  public void createMultiple() throws Exception {
    InvoiceDto invoiceDto1 =
        new InvoiceDto(
            121L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            null,
            "aCompany",
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    MvcResult mvcResult1 = create(invoiceDto1, HttpStatus.CREATED);

    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoice(InvoiceDto.builder().number(122L).build())
            .build();
    InvoiceDto invoiceDto2 =
        new InvoiceDto(
            122L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            List.of(item),
            "bCompany",
            PaymentStatus.UNPAID,
            new BigDecimal(130.00));
    MvcResult mvcResult2 = create(invoiceDto2, HttpStatus.CREATED);

    InvoiceDto createdInvoiceCto =
        objectMapper.readValue(mvcResult1.getResponse().getContentAsString(), InvoiceDto.class);
    assertThat(createdInvoiceCto, is(invoiceDto1));

    List<Item> items =
        invoiceDto2.getItems().stream()
            .map(
                e -> {
                  e.setInvoice(null);
                  return e;
                })
            .collect(Collectors.toList());
    invoiceDto2.setItems(items);

    createdInvoiceCto =
        objectMapper.readValue(mvcResult2.getResponse().getContentAsString(), InvoiceDto.class);

    invoiceDto2.setTotalCost(invoiceDto2.getTotalCost().add(item.getTotalFees()));
    assertThat(createdInvoiceCto, is(invoiceDto2));
  }

  @Test
  public void viewZeroInvoice() throws Exception {
    mockMvc
        .perform(get("/invoices/summary").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void createAndViewInvoiceSummary() throws Exception {

    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoice(InvoiceDto.builder().number(120L).build())
            .build();

    InvoiceDto invoiceDto =
        new InvoiceDto(
            121L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            List.of(item),
            "aCompany",
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    create(invoiceDto, HttpStatus.CREATED);

    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices/summary").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "getInvoiceSummary",
                    responseFields(
                        fieldWithPath("[0].number").description("Invoice number (mandatory)"),
                        fieldWithPath("[0].creationDate").description("Invoice creation date"),
                        fieldWithPath("[0].paymentStatus").description("Invoice payment status"),
                        fieldWithPath("[0].totalCost").description("Invoice total cost"))))
            .andReturn();

    List<InvoiceSummaryDto> dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(),
            new TypeReference<List<InvoiceSummaryDto>>() {});

    assertThat(dtos.get(0).getNumber(), is(invoiceDto.getNumber()));
    assertThat(dtos.get(0).getCreationDate(), is(invoiceDto.getCreationDate()));
  }

  @Test
  public void createAndViewInvoiceDetail() throws Exception {
    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoice(InvoiceDto.builder().number(121L).build())
            .build();

    InvoiceDto invoiceDto1 =
        new InvoiceDto(
            120L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            null,
            "aCompany",
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(200.1).setScale(2));
    create(invoiceDto1, HttpStatus.CREATED);

    InvoiceDto invoiceDto2 =
        new InvoiceDto(
            121L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            List.of(item),
            "bCompany",
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(121.1).setScale(2));
    create(invoiceDto2, HttpStatus.CREATED);

    invoiceDto1.setItems(List.of());
    invoiceDto2.setItems(List.of());

    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "getInvoiceDetail",
                    responseFields(
                        fieldWithPath("[0].number").description("Invoice number (mandatory)"),
                        fieldWithPath("[0].creationDate").description("Invoice creation date"),
                        fieldWithPath("[0].lastModifiedDate").description("Invoice modified date"),
                        fieldWithPath("[0].items").description("items added to this invoice"),
                        fieldWithPath("[0].companyName").description("Company Name"),
                        fieldWithPath("[0].paymentStatus").description("Invoice payment status"),
                        fieldWithPath("[0].totalCost").description("Invoice total cost"))))
            .andReturn();

    List<InvoiceDto> dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<InvoiceDto>>() {});

    assertThat(dtos.size(), is(2));

    invoiceDto2.setTotalCost(invoiceDto2.getTotalCost().add(item.getTotalFees()));
    assertThat(dtos, contains(invoiceDto1, invoiceDto2));
  }

  @Test
  public void searchInvoicesByNumber() throws Exception {
    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoice(InvoiceDto.builder().number(121L).build())
            .build();

    InvoiceDto invoiceDto1 =
        new InvoiceDto(
            120L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            null,
            "aCompany",
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(200.1).setScale(2));
    create(invoiceDto1, HttpStatus.CREATED);

    InvoiceDto invoiceDto2 =
        new InvoiceDto(
            121L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            List.of(item),
            "bCompany",
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(121.1).setScale(2));
    create(invoiceDto2, HttpStatus.CREATED);

    invoiceDto1.setItems(List.of());
    invoiceDto2.setItems(List.of());

    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices/search/121").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "searchInvoiceByNumber",
                    responseFields(
                        fieldWithPath("[0].number").description("Invoice number (mandatory)"),
                        fieldWithPath("[0].creationDate").description("Invoice creation date"),
                        fieldWithPath("[0].lastModifiedDate").description("Invoice modified date"),
                        fieldWithPath("[0].items").description("items added to this invoice"),
                        fieldWithPath("[0].companyName").description("Company Name"),
                        fieldWithPath("[0].paymentStatus").description("Invoice payment status"),
                        fieldWithPath("[0].totalCost").description("Invoice total cost"))))
            .andReturn();

    List<InvoiceDto> dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<InvoiceDto>>() {});

    assertThat(dtos.size(), is(1));

    invoiceDto2.setTotalCost(invoiceDto2.getTotalCost().add(item.getTotalFees()));
    assertThat(dtos, contains(invoiceDto2));
  }

  @Test
  public void upateAndViewInvoiceDetails() throws Exception {
    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoice(InvoiceDto.builder().number(121L).build())
            .build();

    InvoiceDto invoiceDto1 =
        new InvoiceDto(
            120L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            null,
            "aCompany",
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(200.1).setScale(2));
    create(invoiceDto1, HttpStatus.CREATED);

    // update paid status
    invoiceDto1.setPaymentStatus(PaymentStatus.PAID);
    this.update(invoiceDto1, HttpStatus.NO_CONTENT).andDo(document("updateInvoice")).andReturn();

    invoiceDto1.setItems(List.of());

    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices/search/120").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "searchInvoiceByNumber",
                    responseFields(
                        fieldWithPath("[0].number").description("Invoice number (mandatory)"),
                        fieldWithPath("[0].creationDate").description("Invoice creation date"),
                        fieldWithPath("[0].lastModifiedDate").description("Invoice modified date"),
                        fieldWithPath("[0].items").description("items added to this invoice"),
                        fieldWithPath("[0].companyName").description("Company Name"),
                        fieldWithPath("[0].paymentStatus").description("Invoice payment status"),
                        fieldWithPath("[0].totalCost").description("Invoice total cost"))))
            .andReturn();

    List<InvoiceDto> dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<InvoiceDto>>() {});

    // modifed date will be changed due to update
    invoiceDto1.setLastModifiedDate(InvoicifyUtilities.getDate(LocalDate.now()));
    assertThat(dtos.size(), is(1));
    assertThat(dtos, contains(invoiceDto1));
  }

  @Test
  public void deleteInvoices() throws Exception {
    InvoiceDto undeleteInvoiceDto =
        new InvoiceDto(
            120L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            null,
            "aCompany",
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    create(undeleteInvoiceDto, HttpStatus.CREATED);

    InvoiceDto undeleteInvoiceDto2 =
        new InvoiceDto(
            121L,
            InvoicifyUtilities.getDate(LocalDate.now()),
            null,
            null,
            "aCompany",
            PaymentStatus.PAID,
            new BigDecimal(120.00));
    create(undeleteInvoiceDto2, HttpStatus.CREATED);

    InvoiceDto undeleteInvoiceDto3 =
        new InvoiceDto(
            122L,
            InvoicifyUtilities.getDate(LocalDate.now().minusYears(1L).minusDays(1L)),
            null,
            null,
            "aCompany",
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    create(undeleteInvoiceDto3, HttpStatus.CREATED);

    InvoiceDto deleteInvoiceDto4 =
        new InvoiceDto(
            123L,
            InvoicifyUtilities.getDate(LocalDate.now().minusYears(1L).minusDays(1L)),
            null,
            List.of(
                new Item(
                    "aItem",
                    1,
                    BigDecimal.valueOf(10).setScale(2),
                    InvoiceDto.builder().number(123L).build())),
            "aCompany",
            PaymentStatus.PAID,
            new BigDecimal(120.00));
    create(deleteInvoiceDto4, HttpStatus.CREATED);

    mockMvc
        .perform(delete("/invoices").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andReturn();
  }
}
