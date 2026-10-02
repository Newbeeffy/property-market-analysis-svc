package com.interview.market.service;

import com.interview.market.data.PropertyRepository;
import com.interview.market.model.Property;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/** Exports the in-memory dataset as CSV or PDF. */
@Service
public class ExportService {

    private static final String[] HEADERS = {
            "id", "square_footage", "bedrooms", "bathrooms", "year_built",
            "lot_size", "distance_to_city_center", "school_rating", "price"};

    private final PropertyRepository repository;

    public ExportService(PropertyRepository repository) {
        this.repository = repository;
    }

    public String csv() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", HEADERS)).append('\n');
        for (Property p : repository.all()) {
            sb.append(p.id()).append(',')
                    .append(num(p.squareFootage())).append(',')
                    .append(p.bedrooms()).append(',')
                    .append(num(p.bathrooms())).append(',')
                    .append(p.yearBuilt()).append(',')
                    .append(num(p.lotSize())).append(',')
                    .append(num(p.distanceToCityCenter())).append(',')
                    .append(num(p.schoolRating())).append(',')
                    .append(num(p.price())).append('\n');
        }
        return sb.toString();
    }

    public byte[] pdf() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Property Market Data"));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(HEADERS.length);
            table.setWidthPercentage(100);
            for (String header : HEADERS) {
                table.addCell(header);
            }
            for (Property p : repository.all()) {
                table.addCell(String.valueOf(p.id()));
                table.addCell(num(p.squareFootage()));
                table.addCell(String.valueOf(p.bedrooms()));
                table.addCell(num(p.bathrooms()));
                table.addCell(String.valueOf(p.yearBuilt()));
                table.addCell(num(p.lotSize()));
                table.addCell(num(p.distanceToCityCenter()));
                table.addCell(num(p.schoolRating()));
                table.addCell(num(p.price()));
            }
            document.add(table);
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate PDF", e);
        } finally {
            document.close();
        }
        return out.toByteArray();
    }

    /** Renders an integral double without a trailing {@code .0}. */
    private static String num(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
