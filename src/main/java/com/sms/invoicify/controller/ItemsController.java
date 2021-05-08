package com.sms.invoicify.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemsController {


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<String> getAllItems(){
        return List.of();

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String postItem(){
        return "Test Item Description created Successfully";
    }

}
