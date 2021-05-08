package com.sms.invoicify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.invoicify.models.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemIT {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getItemsWhenEmpty() throws Exception{
        mockMvc.perform(get("/items"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("length()").value(0));

    }

    @Test
    void postItem() throws Exception{
        Item item =  Item.builder().description("Test Item Description").quantity(1).totalFees(BigDecimal.TEN).build();
        mockMvc.perform(post("/items").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(item)))
        .andExpect(status().isCreated())
        .andExpect(content().string("Test Item Description created Successfully"));

    }






}
