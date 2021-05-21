package com.sms.invoicify.controller;

import com.sms.invoicify.exception.InvoicifyInvoiceNotExistsException;
import com.sms.invoicify.models.Item;
import com.sms.invoicify.service.ItemService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemsController {

  ItemService itemService;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<Item> getAllItems() {
    return itemService.fetchAllItems();
  }

  @PostMapping
  public ResponseEntity<String> postItem(@RequestBody Item item)
      throws InvoicifyInvoiceNotExistsException {
    try {
      itemService.createItem(item);
      return new ResponseEntity<String>(
          item.getDescription() + " created Successfully", HttpStatus.CREATED);
    } catch (InvoicifyInvoiceNotExistsException e) {
      return new ResponseEntity<String>("Invoice does not exist. Please use valid invoice.", HttpStatus.BAD_REQUEST);
    }
  }
}
