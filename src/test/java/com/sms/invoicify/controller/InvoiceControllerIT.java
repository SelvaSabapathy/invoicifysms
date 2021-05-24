package com.sms.invoicify.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.invoicify.models.Address;
import com.sms.invoicify.models.Company;
import com.sms.invoicify.models.Invoice;
import com.sms.invoicify.models.InvoiceSummary;
import com.sms.invoicify.models.Item;
import com.sms.invoicify.utilities.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseBody;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
public class InvoiceControllerIT {

  private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

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

  private void createCompany(String companyName) throws Exception {
    Company company =
        Company.builder()
            .companyName(companyName)
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
        .andExpect(content().string(companyName + " created Successfully"));
  }

  private MvcResult create(Invoice invoice, HttpStatus status) throws Exception {

    MvcResult mvcResult =
        this.createInner(invoice, status)
            .andDo(
                document(
                    "{class-name}/{method-name}/{step}",
                    relaxedRequestFields(
                        attributes(key("title").value("Fields for Item Creation")),
                        fieldWithPath("number")
                            .description("Invoice ID Number")
                            .attributes(key("constraints").value("Not Null")),
                        fieldWithPath("creationDate")
                            .description("Date timestamp when Invoice Created")
                            .attributes(key("constraints").value(""))
                            .ignored(),
                        fieldWithPath("lastModifiedDate")
                            .description("Date timestamp of Last Modification")
                            .attributes(key("constraints").value(""))
                            .ignored(),
                        fieldWithPath("items")
                            .description("List of Items Associated with the invoice")
                            .attributes(key("constraints").value(""))
                            .ignored(),
                        fieldWithPath("companyName")
                            .description("Company Billable for the Invoice")
                            .attributes(key("constraints").value("Not Null, Company Name must match an Existing Saved Company")),
                        paymentStatusField(),
                        fieldWithPath("totalCost")
                            .description("Sum of All Line Item Charges on the Invoice")
                            .attributes(key("constraints").value("Not Null"))),
                    responseBody()))
            .andReturn();
    return mvcResult;
  }

  private FieldDescriptor paymentStatusField() {
    String formattedValues =
        Arrays.stream(PaymentStatus.values())
            .map(type -> String.format("`%s`", type))
            .collect(Collectors.joining(", "));
    return fieldWithPath("paymentStatus")
        .description("The Current ENUM Payment status of the invoice.")
        .attributes(key("constraints").value("Enumerated, One of: " + formattedValues));
  }

  private ResultActions createInner(Invoice invoice, HttpStatus status) throws Exception {
    return mockMvc
        .perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoice)))
        .andExpect(status().is(status.value()));
  }

  private ResultActions update(Invoice invoice, HttpStatus status) throws Exception {
    return mockMvc
        .perform(
            put("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoice)))
        .andExpect(status().is(status.value()));
  }

  @Test
  public void createOneFailureNullNumber() throws Exception {
    Invoice invoice =
        new Invoice(
            null,
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
                .content(objectMapper.writeValueAsString(invoice)))
        .andExpect(status().isBadRequest())
        .andDo(document("{class-name}/{method-name}/{step}"))
        .andReturn();
  }

  @Test
  public void createOneFailureExistingNumber() throws Exception {
    String companyName = "aCompany";
    createCompany(companyName);

    Invoice invoice =
        new Invoice(
            121L,
            LocalDate.now(),
            null,
            null,
            companyName,
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    MvcResult mvcResult = create(invoice, HttpStatus.CREATED);

    mockMvc
        .perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoice)))
        .andExpect(status().isBadRequest())
        .andDo(document("{class-name}/{method-name}/{step}"))
        .andReturn();
  }

  @Test
  public void createOneSuccess() throws Exception {
    String companyName = "aCompany";
    createCompany(companyName);

    Invoice invoice =
        new Invoice(
            121L,
            LocalDate.now(),
            null,
            null,
            companyName,
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    MvcResult mvcResult = create(invoice, HttpStatus.CREATED);

    Invoice createdInvoiceCto =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Invoice.class);

    assertThat(createdInvoiceCto, is(invoice));
  }

  @Test
  public void createMultiple() throws Exception {
    String companyName = "aCompany";
    createCompany(companyName);

    Invoice invoice1 =
        new Invoice(
            121L,
            LocalDate.now(),
            null,
            null,
            companyName,
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    MvcResult mvcResult1 = create(invoice1, HttpStatus.CREATED);

    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoiceNumber(122L)
            .build();
    Invoice invoice2 =
        new Invoice(
            122L,
            LocalDate.now(),
            null,
            List.of(item),
            companyName,
            PaymentStatus.UNPAID,
            new BigDecimal(130.00));
    MvcResult mvcResult2 = create(invoice2, HttpStatus.CREATED);

    Invoice createdInvoiceCto =
        objectMapper.readValue(mvcResult1.getResponse().getContentAsString(), Invoice.class);
    assertThat(createdInvoiceCto, is(invoice1));

    List<Item> items =
        invoice2.getItems().stream()
            .map(
                e -> {
                  e.setInvoiceNumber(null);
                  return e;
                })
            .collect(Collectors.toList());
    invoice2.setItems(items);

    createdInvoiceCto =
        objectMapper.readValue(mvcResult2.getResponse().getContentAsString(), Invoice.class);

    invoice2.setTotalCost(invoice2.getTotalCost().add(item.getTotalFees()));
    assertThat(createdInvoiceCto, is(invoice2));
  }

  @Test
  public void viewZeroInvoice() throws Exception {
    mockMvc
        .perform(get("/invoices/summary").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void createAndViewInvoiceSummary() throws Exception {
    String companyName = "aCompany";
    createCompany(companyName);

    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoiceNumber(120L)
            .build();

    Invoice invoice =
        new Invoice(
            121L,
            LocalDate.now(),
            null,
            List.of(item),
            companyName,
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    create(invoice, HttpStatus.CREATED);

    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices/summary").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "{class-name}/{method-name}/{step}",
                    responseFields(
                        fieldWithPath("[0].number").description("Invoice number (mandatory)"),
                        fieldWithPath("[0].creationDate").description("Invoice creation date"),
                        fieldWithPath("[0].paymentStatus").description("Invoice payment status"),
                        fieldWithPath("[0].totalCost").description("Invoice total cost"))))
            .andReturn();

    List<InvoiceSummary> dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(),
            new TypeReference<List<InvoiceSummary>>() {});

    assertThat(dtos.get(0).getNumber(), is(invoice.getNumber()));
    assertThat(dtos.get(0).getCreationDate(), is(invoice.getCreationDate()));
  }

  @Test
  public void createAndViewInvoiceDetail() throws Exception {
    String companyName = "aCompany";
    createCompany(companyName);

    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoiceNumber(121L)
            .build();

    Invoice invoice1 =
        new Invoice(
            120L,
            LocalDate.now().minusDays(1),
            null,
            null,
            companyName,
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(200.1).setScale(2));
    create(invoice1, HttpStatus.CREATED);

    Invoice invoice2 =
        new Invoice(
            121L,
            LocalDate.now(),
            null,
            List.of(item),
            companyName,
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(121.1).setScale(2));
    create(invoice2, HttpStatus.CREATED);

    invoice1.setItems(List.of());
    invoice2.setItems(List.of());

    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "{class-name}/{method-name}/{step}",
                    relaxedResponseFields(
                        fieldWithPath("[0].number").description("Invoice number (mandatory)"),
                        fieldWithPath("[0].creationDate").description("Invoice creation date"),
                        fieldWithPath("[0].lastModifiedDate").description("Invoice modified date"),
                        fieldWithPath("[0].items").description("items added to this invoice"),
                        fieldWithPath("[0].companyName").description("Company Name"),
                        fieldWithPath("[0].paymentStatus").description("Invoice payment status"),
                        fieldWithPath("[0].totalCost").description("Invoice total cost"))))
            .andReturn();

    List<Invoice> dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {});

    assertThat(dtos.size(), is(2));

    invoice2.setTotalCost(invoice2.getTotalCost().add(item.getTotalFees()));
    assertThat(dtos, contains(invoice1, invoice2));
  }

  @Test
  public void searchInvoicesByNumber() throws Exception {
    String companyName = "aCompany";
    createCompany(companyName);

    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoiceNumber(121L)
            .build();

    Invoice invoice1 =
        new Invoice(
            120L,
            LocalDate.now(),
            null,
            null,
            companyName,
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(200.1).setScale(2));
    create(invoice1, HttpStatus.CREATED);

    Invoice invoice2 =
        new Invoice(
            121L,
            LocalDate.now(),
            null,
            List.of(item),
            companyName,
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(121.1).setScale(2));
    create(invoice2, HttpStatus.CREATED);

    invoice1.setItems(List.of());
    invoice2.setItems(List.of());

    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices/search/121").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "{class-name}/{method-name}/{step}",
                    responseFields(
                        fieldWithPath("[0].number").description("Invoice number (mandatory)"),
                        fieldWithPath("[0].creationDate").description("Invoice creation date"),
                        fieldWithPath("[0].lastModifiedDate").description("Invoice modified date"),
                        fieldWithPath("[0].items").description("items added to this invoice"),
                        fieldWithPath("[0].companyName").description("Company Name"),
                        fieldWithPath("[0].paymentStatus").description("Invoice payment status"),
                        fieldWithPath("[0].totalCost").description("Invoice total cost"))))
            .andReturn();

    List<Invoice> dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {});

    assertThat(dtos.size(), is(1));

    invoice2.setTotalCost(invoice2.getTotalCost().add(item.getTotalFees()));
    assertThat(dtos, contains(invoice2));
  }

  @Test
  public void updateAndViewInvoiceDetails() throws Exception {
    String companyName = "aCompany";
    createCompany(companyName);

    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoiceNumber(121L)
            .build();

    Invoice invoice1 =
        new Invoice(
            120L,
            LocalDate.now(),
            null,
            null,
            companyName,
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(200.1).setScale(2));
    create(invoice1, HttpStatus.CREATED);

    // update paid status
    invoice1.setPaymentStatus(PaymentStatus.PAID);
    this.update(invoice1, HttpStatus.NO_CONTENT)
        .andDo(document("{class-name}/{method-name}/{step}"))
        .andReturn();

    invoice1.setItems(List.of());

    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices/search/120").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "{class-name}/{method-name}/{step}",
                    responseFields(
                        fieldWithPath("[0].number").description("Invoice number (mandatory)"),
                        fieldWithPath("[0].creationDate").description("Invoice creation date"),
                        fieldWithPath("[0].lastModifiedDate").description("Invoice modified date"),
                        fieldWithPath("[0].items").description("items added to this invoice"),
                        fieldWithPath("[0].companyName").description("Company Name"),
                        fieldWithPath("[0].paymentStatus").description("Invoice payment status"),
                        fieldWithPath("[0].totalCost").description("Invoice total cost"))))
            .andReturn();

    List<Invoice> dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {});

    // modifed date will be changed due to update
    invoice1.setLastModifiedDate(LocalDate.now());
    assertThat(dtos.size(), is(1));
    assertThat(dtos, contains(invoice1));
  }

  @Test
  public void deleteInvoices() throws Exception {
    String companyName = "aCompany";
    createCompany(companyName);

    Invoice undeleteInvoice =
        new Invoice(
            120L,
            LocalDate.now().minusDays(1),
            null,
            null,
            companyName,
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    create(undeleteInvoice, HttpStatus.CREATED);

    Invoice undeleteInvoice2 =
        new Invoice(
            121L,
            LocalDate.now(),
            null,
            null,
            companyName,
            PaymentStatus.PAID,
            new BigDecimal(120.00));
    create(undeleteInvoice2, HttpStatus.CREATED);

    Invoice undeleteInvoice3 =
        new Invoice(
            122L,
            LocalDate.now().minusYears(1L).minusDays(1L),
            null,
            null,
            companyName,
            PaymentStatus.UNPAID,
            new BigDecimal(120.00));
    create(undeleteInvoice3, HttpStatus.CREATED);

    Invoice deleteInvoice4 =
        new Invoice(
            123L,
            LocalDate.now().minusYears(1L).minusDays(1L),
            null,
            null,
            companyName,
            PaymentStatus.PAID,
            new BigDecimal(120.00));
    create(deleteInvoice4, HttpStatus.CREATED);

    // Create an item and attach it to an invoice which is getting deleted
    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Item.builder()
                            .description("Test Item Description")
                            .quantity(1)
                            .totalFees(BigDecimal.valueOf(10).setScale(2))
                            .invoiceNumber(123L)
                            .build())))
        .andExpect(status().isCreated());

    // Create an item and attach it to an invoice which is not getting deleted
    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Item.builder()
                            .description("Test Item Description-Invoice 120")
                            .quantity(1)
                            .totalFees(BigDecimal.valueOf(100).setScale(2))
                            .invoiceNumber(120L)
                            .build())))
        .andExpect(status().isCreated());

    // verify our setup before deleting -- item & invoice
    mockMvc
        .perform(get("/invoices").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(4));

    mockMvc
        .perform(get("/items").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(2));

    // delete
    mockMvc
        .perform(delete("/invoices").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andDo(document("{class-name}/{method-name}/{step}"));

    // verify after delete -- item & invoice
    mockMvc
        .perform(get("/invoices").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(3))
        .andExpect(jsonPath("[0].number").value(122L))
        .andExpect(jsonPath("[1].number").value(120L))
        .andExpect(jsonPath("[2].number").value(121L));

    mockMvc
        .perform(get("/items").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("length()").value(1))
        .andExpect(jsonPath("[0].description").value("Test Item Description-Invoice 120"));
  }

  private List<Invoice> createAndViewUnpaidInvoices(
      String aCompanyName, String bCompanyName, BigDecimal itemCost) throws Exception {
    createCompany(aCompanyName);
    createCompany(bCompanyName);

    Item item =
        Item.builder()
            .description("Test Item Description")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoiceNumber(121L)
            .build();

    Invoice invoice1 =
        new Invoice(
            120L,
            LocalDate.now(),
            null,
            null,
            aCompanyName,
            PaymentStatus.PAID,
            BigDecimal.valueOf(200.1).setScale(2));
    create(invoice1, HttpStatus.CREATED);

    Invoice invoice2 =
        new Invoice(
            121L,
            LocalDate.now().minusDays(3),
            null,
            List.of(item),
            aCompanyName,
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(121.1).setScale(2));
    create(invoice2, HttpStatus.CREATED);

    Invoice invoice3 =
        new Invoice(
            122L,
            LocalDate.now(),
            null,
            List.of(item),
            bCompanyName,
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(122.1).setScale(2));
    create(invoice3, HttpStatus.CREATED);

    Invoice invoice4 =
        new Invoice(
            123L,
            LocalDate.now().minusDays(4),
            null,
            List.of(item),
            aCompanyName,
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(121.1).setScale(2));
    create(invoice4, HttpStatus.CREATED);

    Invoice invoice5 =
        new Invoice(
            125L,
            LocalDate.now().minusDays(2),
            null,
            List.of(item),
            aCompanyName,
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(121.1).setScale(2));
    create(invoice5, HttpStatus.CREATED);

    Invoice invoice6 =
        new Invoice(
            126L,
            LocalDate.now().minusDays(1),
            null,
            List.of(item),
            aCompanyName,
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(121.1).setScale(2));
    create(invoice6, HttpStatus.CREATED);

    Invoice invoice7 =
        new Invoice(
            127L,
            LocalDate.now(),
            null,
            List.of(item),
            aCompanyName,
            PaymentStatus.UNPAID,
            BigDecimal.valueOf(121.1).setScale(2));
    create(invoice7, HttpStatus.CREATED);

    invoice1.setItems(List.of());
    invoice2.setItems(List.of());
    invoice3.setItems(List.of());
    invoice4.setItems(List.of());
    invoice5.setItems(List.of());
    invoice6.setItems(List.of());
    invoice7.setItems(List.of());

    return List.of(
            invoice1, invoice2, invoice3, invoice4, invoice5, invoice6, invoice7);
  }

  @Test
  public void createAndViewUnpaidInvoiceDetail() throws Exception {
    BigDecimal itemCost = BigDecimal.TEN;
    List<Invoice> createdInvoices =
        createAndViewUnpaidInvoices("aCompany", "bCompany", itemCost);
    Invoice invoice2 = createdInvoices.get(1);
    Invoice invoice4 = createdInvoices.get(3);
    Invoice invoice5 = createdInvoices.get(4);
    Invoice invoice6 = createdInvoices.get(5);
    Invoice invoice7 = createdInvoices.get(6);

    // Get  invoiceDto4, followed by invoiceDto2
    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/invoices/unpaid/aCompany?pageNumber=0&pageSize=2")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "{class-name}/{method-name}/{step}",
                    relaxedResponseFields(
                        fieldWithPath("[0].number").description("Invoice number (mandatory)"),
                        fieldWithPath("[0].creationDate").description("Invoice creation date"),
                        fieldWithPath("[0].lastModifiedDate").description("Invoice modified date"),
                        fieldWithPath("[0].items").description("items added to this invoice"),
                        fieldWithPath("[0].companyName").description("Company Name"),
                        fieldWithPath("[0].paymentStatus").description("Invoice payment status"),
                        fieldWithPath("[0].totalCost").description("Invoice total cost"))))
            .andReturn();

    List<Invoice> dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {});

    assertThat(dtos.size(), is(2));

    invoice2.setTotalCost(invoice2.getTotalCost().add(itemCost));
    invoice4.setTotalCost(invoice4.getTotalCost().add(itemCost));
    assertThat(dtos, is(List.of(invoice4, invoice2)));

    // Get  invoiceDto5, followed by invoiceDto6
    mvcResult =
        mockMvc
            .perform(
                get("/invoices/unpaid/aCompany?pageNumber=1&pageSize=2")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {});

    assertThat(dtos.size(), is(2));

    invoice5.setTotalCost(invoice5.getTotalCost().add(itemCost));
    invoice6.setTotalCost(invoice6.getTotalCost().add(itemCost));
    assertThat(dtos, is(List.of(invoice5, invoice6)));

    // Get  invoiceDto7
    mvcResult =
        mockMvc
            .perform(
                get("/invoices/unpaid/aCompany?pageNumber=2&pageSize=2")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {});

    assertThat(dtos.size(), is(1));

    invoice7.setTotalCost(invoice7.getTotalCost().add(itemCost));
    assertThat(dtos, is(List.of(invoice7)));
  }

  @Test
  public void createAndViewUnpaidInvoiceSummary() throws Exception {
    BigDecimal itemCost = BigDecimal.TEN;
    List<Invoice> createdInvoices =
        createAndViewUnpaidInvoices("aCompany", "bCompany", itemCost);
    Invoice invoice2 = createdInvoices.get(1);
    Invoice invoice4 = createdInvoices.get(3);
    Invoice invoice5 = createdInvoices.get(4);
    Invoice invoice6 = createdInvoices.get(5);
    Invoice invoice7 = createdInvoices.get(6);

    // Get  invoiceDto4, followed by invoiceDto2
    MvcResult mvcResult =
        mockMvc
            .perform(
                get("/invoices/summary/unpaid/aCompany?pageNumber=0&pageSize=2")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "{class-name}/{method-name}/{step}",
                    relaxedResponseFields(
                        fieldWithPath("[0].number").description("Invoice number (mandatory)"),
                        fieldWithPath("[0].creationDate").description("Invoice creation date"),
                        fieldWithPath("[0].paymentStatus").description("Invoice payment status"),
                        fieldWithPath("[0].totalCost").description("Invoice total cost"))))
            .andReturn();

    List<Invoice> dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {});

    assertThat(dtos.size(), is(2));

    assertThat(dtos.get(0).getNumber(), is(invoice4.getNumber()));
    assertThat(dtos.get(0).getPaymentStatus(), is(invoice4.getPaymentStatus()));
    assertThat(dtos.get(1).getNumber(), is(invoice2.getNumber()));
    assertThat(dtos.get(1).getPaymentStatus(), is(invoice2.getPaymentStatus()));

    // Get  invoiceDto5, followed by invoiceDto6
    mvcResult =
        mockMvc
            .perform(
                get("/invoices/unpaid/aCompany?pageNumber=1&pageSize=2")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {});

    assertThat(dtos.size(), is(2));

    assertThat(dtos.get(0).getNumber(), is(invoice5.getNumber()));
    assertThat(dtos.get(0).getPaymentStatus(), is(invoice5.getPaymentStatus()));
    assertThat(dtos.get(1).getNumber(), is(invoice6.getNumber()));
    assertThat(dtos.get(1).getPaymentStatus(), is(invoice6.getPaymentStatus()));

    // Get  invoiceDto7
    mvcResult =
        mockMvc
            .perform(
                get("/invoices/unpaid/aCompany?pageNumber=2&pageSize=2")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {});

    assertThat(dtos.size(), is(1));

    assertThat(dtos.get(0).getNumber(), is(invoice7.getNumber()));
    assertThat(dtos.get(0).getPaymentStatus(), is(invoice7.getPaymentStatus()));
  }

  @Test
  void findALlInvoices_returnsSortedListByCreationDateAsc() throws Exception {

    createCompany("Company");

    Item item1 =
        Item.builder()
            .description("MIDDLE DATE")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoiceNumber(121L)
            .build();

    Item item2 =
        Item.builder()
            .description("LAST DATE")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoiceNumber(121L)
            .build();

    Item item3 =
        Item.builder()
            .description("EARLIEST DATE")
            .quantity(1)
            .totalFees(BigDecimal.TEN)
            .invoiceNumber(121L)
            .build();

    Invoice invoice1 =
        Invoice.builder()
            .companyName("Company")
            .number(1L)
            .paymentStatus(PaymentStatus.PAID)
            .items(List.of(item1))
            .creationDate(LocalDate.of(2021, 05, 22))
            .build();
    Invoice invoice2 =
        Invoice.builder()
            .companyName("Company")
            .items(List.of(item2))
            .number(2L)
            .paymentStatus(PaymentStatus.PAID)
            .creationDate(LocalDate.of(2021, 05, 23))
            .build();
    Invoice invoice3 =
        Invoice.builder()
            .companyName("Company")
            .items(List.of(item3))
            .number(3L)
            .paymentStatus(PaymentStatus.PAID)
            .creationDate(LocalDate.of(2021, 04, 22))
            .build();

    this.createInner(invoice1, HttpStatus.CREATED);
    this.createInner(invoice2, HttpStatus.CREATED);
    this.createInner(invoice3, HttpStatus.CREATED);

    mockMvc
        .perform(get("/invoices?pageNumber=0&pageSize=2"))
        .andExpect(jsonPath("length()").value(2))
        .andExpect(jsonPath("[0].number").value(3))
        .andExpect(jsonPath("[1].number").value(1));

    // second page
    mockMvc
        .perform(get("/invoices?pageNumber=1&pageSize=2"))
        .andExpect(jsonPath("length()").value(1))
        .andExpect(jsonPath("[0].number").value(2));
  }

  @Test
  void findALlInvoices_returnsSummarySortedListByCreationDateAsc() throws Exception {
    createCompany("Company");

    Item item1 =
            Item.builder()
                    .description("MIDDLE DATE")
                    .quantity(1)
                    .totalFees(BigDecimal.TEN)
                    .invoiceNumber(121L)
                    .build();

    Item item2 =
            Item.builder()
                    .description("LAST DATE")
                    .quantity(1)
                    .totalFees(BigDecimal.TEN)
                    .invoiceNumber(121L)
                    .build();

    Item item3 =
            Item.builder()
                    .description("EARLIEST DATE")
                    .quantity(1)
                    .totalFees(BigDecimal.TEN)
                    .invoiceNumber(121L)
                    .build();

    Invoice invoice1 =
            Invoice.builder()
                    .companyName("Company")
                    .number(1L)
                    .paymentStatus(PaymentStatus.PAID)
                    .items(List.of(item1))
                    .creationDate(LocalDate.of(2021, 05, 22))
                    .build();
    Invoice invoice2 =
            Invoice.builder()
                    .companyName("Company")
                    .items(List.of(item2))
                    .number(2L)
                    .paymentStatus(PaymentStatus.PAID)
                    .creationDate(LocalDate.of(2021, 05, 23))
                    .build();
    Invoice invoice3 =
            Invoice.builder()
                    .companyName("Company")
                    .items(List.of(item3))
                    .number(3L)
                    .paymentStatus(PaymentStatus.PAID)
                    .creationDate(LocalDate.of(2021, 04, 22))
                    .build();

    this.createInner(invoice1, HttpStatus.CREATED);
    this.createInner(invoice2, HttpStatus.CREATED);
    this.createInner(invoice3, HttpStatus.CREATED);

    mockMvc
            .perform(get("/invoices/summary?pageNumber=0&pageSize=2"))
            .andExpect(jsonPath("length()").value(2))
            .andExpect(jsonPath("[0].number").value(3))
            .andExpect(jsonPath("[1].number").value(1));

    // second page
    mockMvc
            .perform(get("/invoices/summary?pageNumber=1&pageSize=2"))
            .andExpect(jsonPath("length()").value(1))
            .andExpect(jsonPath("[0].number").value(2));
  }

  @Test
  void createInvoiceWithMultipleItems() throws Exception {
    createCompany("COMPANY_NAME");

    Invoice invoice =
        Invoice.builder()
            .companyName("COMPANY_NAME")
            .number(42L)
            .paymentStatus(PaymentStatus.UNPAID)
            .items(generateItemsList(42L))
            .build();

    mockMvc
        .perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invoice)))
        .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.companyName", is("COMPANY_NAME")))
            .andExpect(jsonPath("$.items.[0].description", is("FIRST_ITEM")))
            .andExpect(jsonPath("$.items.[1].description", is("SECOND_ITEM")))
            .andExpect(jsonPath("$.items.[2].description", is("THIRD_ITEM")));
  }

  private List<Item> generateItemsList(Long invoiceNumber) {
    Item item1 =
        Item.builder()
            .description("FIRST_ITEM")
            .quantity(5)
            .totalFees(BigDecimal.valueOf(25.00))
            .invoiceNumber(invoiceNumber)
            .build();

    Item item2 =
        Item.builder()
            .description("SECOND_ITEM")
            .quantity(1)
            .totalFees(BigDecimal.valueOf(45.00))
            .invoiceNumber(invoiceNumber)
            .build();

    Item item3 =
        Item.builder()
            .description("THIRD_ITEM")
            .quantity(1)
            .totalFees(BigDecimal.valueOf(30.99))
            .invoiceNumber(invoiceNumber)
            .build();

    return List.of(item1, item2, item3);
  }
}
