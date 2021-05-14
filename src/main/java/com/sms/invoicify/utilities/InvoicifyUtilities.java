package com.sms.invoicify.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class InvoicifyUtilities {

  public static Date getDate(LocalDate localDate) throws ParseException {
    Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    Date res = Date.from(instant);

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    return formatter.parse(formatter.format(res));
  }
}
