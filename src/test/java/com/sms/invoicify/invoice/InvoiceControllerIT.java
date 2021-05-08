package com.sms.invoicify.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Date;
import java.time.LocalDate;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class InvoiceControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @BeforeAll
  public static void setup() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  private MvcResult create(InvoiceDto invoiceDto) throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(
                post("/invoices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invoiceDto)))
            .andExpect(status().isCreated())
            .andReturn();

    return mvcResult;
  }

  @Test
  public void createOne() throws Exception {
    InvoiceDto invoiceDto = new InvoiceDto(121, Date.valueOf(LocalDate.now()), null, "aCompany", PaymentStatus.UNPAID, 120.00);
    MvcResult mvcResult = create(invoiceDto);

    InvoiceDto createdInvoiceCto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), InvoiceDto.class);

    assertThat(createdInvoiceCto, is(invoiceDto));
  }

  @Test
  public void createMultiple() throws Exception {
    InvoiceDto invoiceDto1 = new InvoiceDto(121, Date.valueOf(LocalDate.now()), null, "aCompany", PaymentStatus.UNPAID, 120.00);
    MvcResult mvcResult1 = create(invoiceDto1);

    InvoiceDto invoiceDto2 = new InvoiceDto(122, Date.valueOf(LocalDate.now()), null, "bCompany", PaymentStatus.UNPAID, 130.00);
    MvcResult mvcResult2 = create(invoiceDto2);

    InvoiceDto createdInvoiceCto = objectMapper.readValue(mvcResult1.getResponse().getContentAsString(), InvoiceDto.class);
    assertThat(createdInvoiceCto, is(invoiceDto1));

    createdInvoiceCto = objectMapper.readValue(mvcResult2.getResponse().getContentAsString(), InvoiceDto.class);
    assertThat(createdInvoiceCto, is(invoiceDto2));
  }

  @Test
  public void viewZeroInvoice() throws Exception {
    mockMvc
            .perform(
                    get("/invoices/summary")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
  }
}
