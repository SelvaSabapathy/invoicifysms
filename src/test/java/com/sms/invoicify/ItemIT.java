package com.sms.invoicify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemIT {
    @Autowired
    MockMvc mockMvc;

    @Test
    void getItemsWhenEmpty() throws Exception{
        mockMvc.perform(get("/items"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("length()").value(0));

    }






}
