package com.interview.market.data;

import com.interview.market.model.Property;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Parses the housing dataset CSV into a list of Property records. */
public final class PropertyCsvLoader {

    private PropertyCsvLoader() {
    }

    public static List<Property> load(Resource csv) {
        List<Property> properties = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false; // skip the header row (and its optional BOM)
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                properties.add(parse(line));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load property dataset", e);
        }
        return properties;
    }

    private static Property parse(String line) {
        // Column order: id, square_footage, bedrooms, bathrooms, year_built,
        //               lot_size, distance_to_city_center, school_rating, price
        String[] f = line.split(",");
        return new Property(
                Integer.parseInt(f[0].trim()),
                Double.parseDouble(f[1].trim()),
                Integer.parseInt(f[2].trim()),
                Double.parseDouble(f[3].trim()),
                Integer.parseInt(f[4].trim()),
                Double.parseDouble(f[5].trim()),
                Double.parseDouble(f[6].trim()),
                Double.parseDouble(f[7].trim()),
                Double.parseDouble(f[8].trim()));
    }
}
