package com.sms.invoicify.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
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
            .andReturn();

    return mvcResult;
  }

  @Test
  public void createOne() throws Exception {
    InvoiceDto invoiceDto = new InvoiceDto();
    create(invoiceDto);
  }

  @Test
  public void createMultiple() throws Exception {
    InvoiceDto invoiceDto1 = new InvoiceDto();
    create(invoiceDto1);

    InvoiceDto invoiceDto2 = new InvoiceDto();
    create(invoiceDto2);
  }
}
