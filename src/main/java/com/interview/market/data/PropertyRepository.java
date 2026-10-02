package com.interview.market.data;

import com.interview.market.model.Property;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * In-memory store of the housing dataset, loaded once at startup.
 */
@Component
public class PropertyRepository {

    private final List<Property> properties;

    public PropertyRepository(@Value("classpath:data/house-price-dataset.csv") Resource csv) {
        this.properties = PropertyCsvLoader.load(csv);
    }

    public List<Property> all() {
        return properties;
    }
}
