package com.sms.invoicify.utilities;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class InvoicifyUtilitiesTest {

  @Test
  public void getDateTest() throws ParseException {
    assertThat(InvoicifyUtilities.getDate(LocalDate.now()), is(notNullValue()));
  }
}
