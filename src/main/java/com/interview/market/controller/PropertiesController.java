package com.interview.market.controller;

import com.interview.market.data.PropertyRepository;
import com.interview.market.model.Property;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PropertiesController {

    private final PropertyRepository repository;

    public PropertiesController(PropertyRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/properties")
    public List<Property> properties() {
        return repository.all();
    }
}
