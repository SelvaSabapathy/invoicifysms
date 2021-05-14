package com.sms.invoicify.controller;

import com.sms.invoicify.models.Item;
import com.sms.invoicify.service.ItemService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
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
  @ResponseStatus(HttpStatus.CREATED)
  public String postItem(@RequestBody Item item) throws ParseException {
    itemService.createItem(item);
    return item.getDescription() + " created Successfully";
  }
}
