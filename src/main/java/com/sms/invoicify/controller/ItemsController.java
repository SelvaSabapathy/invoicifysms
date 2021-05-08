package com.sms.invoicify.controller;

import com.sms.invoicify.models.Item;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemsController {

    List<Item> itemList = new ArrayList<>();

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Item> getAllItems(){
        return itemList;

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String postItem(@RequestBody Item item){
        itemList.add(item);
        return item.getDescription() + " created Successfully";

    }

}
