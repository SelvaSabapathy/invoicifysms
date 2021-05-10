package com.sms.invoicify.invoice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class InvoiceControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private MvcResult create(InvoiceDto invoiceDto) throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/invoices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invoiceDto)))
            .andExpect(status().isCreated())
            .andDo(document("createNewInvoice"))
            .andReturn();

    return mvcResult;
  }

  private Date getDate(LocalDate localDate) throws ParseException {
    Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    Date res = Date.from(instant);

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    return formatter.parse(formatter.format(res));
  }

  @Test
  public void createOne() throws Exception {
    InvoiceDto invoiceDto =
        new InvoiceDto(
            121, getDate(LocalDate.now()), null, "aCompany", PaymentStatus.UNPAID, 120.00);
    MvcResult mvcResult = create(invoiceDto);

    InvoiceDto createdInvoiceCto =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), InvoiceDto.class);

    assertThat(createdInvoiceCto, is(invoiceDto));
  }

  @Test
  public void createMultiple() throws Exception {
    InvoiceDto invoiceDto1 =
        new InvoiceDto(
            121, getDate(LocalDate.now()), null, "aCompany", PaymentStatus.UNPAID, 120.00);
    MvcResult mvcResult1 = create(invoiceDto1);

    InvoiceDto invoiceDto2 =
        new InvoiceDto(
            122, getDate(LocalDate.now()), null, "bCompany", PaymentStatus.UNPAID, 130.00);
    MvcResult mvcResult2 = create(invoiceDto2);

    InvoiceDto createdInvoiceCto =
        objectMapper.readValue(mvcResult1.getResponse().getContentAsString(), InvoiceDto.class);
    assertThat(createdInvoiceCto, is(invoiceDto1));

    createdInvoiceCto =
        objectMapper.readValue(mvcResult2.getResponse().getContentAsString(), InvoiceDto.class);
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

    InvoiceDto invoiceDto =
        new InvoiceDto(
            121, getDate(LocalDate.now()), null, "aCompany", PaymentStatus.UNPAID, 120.00);
    create(invoiceDto);

    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices/summary").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "getInvoiceSummary",
                    responseFields(
                        fieldWithPath("[0].number").description("Invoice number"),
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
    InvoiceDto invoiceDto1 =
        new InvoiceDto(
            120, getDate(LocalDate.now()), null, "aCompany", PaymentStatus.UNPAID, 120.00);
    create(invoiceDto1);

    InvoiceDto invoiceDto2 =
        new InvoiceDto(
            121, getDate(LocalDate.now()), null, "bCompany", PaymentStatus.UNPAID, 121.00);
    create(invoiceDto2);

    MvcResult mvcResult =
        mockMvc
            .perform(get("/invoices").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "getInvoiceDetail",
                    responseFields(
                        fieldWithPath("[0].number").description("Invoice number"),
                        fieldWithPath("[0].creationDate").description("Invoice creation date"),
                        fieldWithPath("[0].lastModifiedDate").description("Invoice modified date"),
                        fieldWithPath("[0].companyName").description("Company Name"),
                        fieldWithPath("[0].paymentStatus").description("Invoice payment status"),
                        fieldWithPath("[0].totalCost").description("Invoice total cost"))))
            .andReturn();

    List<InvoiceDto> dtos =
        objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<List<InvoiceDto>>() {});

    assertThat(dtos.size(), is(2));
    assertThat(dtos, contains(invoiceDto1, invoiceDto2));
  }
}
