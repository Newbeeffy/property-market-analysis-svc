# Property Market Analysis Service

A Java (Spring Boot) service that analyses the housing dataset and delegates
"what-if" analysis to the downstream model service (`house-price-predict-svc`,
Task 1).

## Responsibilities

- Aggregate statistics over the dataset (in-memory, loaded from CSV at startup).
- What-if analysis backed by the model service (`/model-info` and `/predict`).
- CSV / PDF export of the dataset.

The dataset is 50 rows and static, so it is loaded into an in-memory `List` and
never needs a database. Filtering, sorting and pagination of the property table
are intentionally left to the frontend.

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check (k8s probe) |
| `/properties` | GET | Full dataset (frontend does its own filtering/sorting/paging) |
| `/market/segments?groupBy=` | GET | Grouped stats by `bedrooms` / `bathrooms` / `yearBuilt` / `schoolRating` |
| `/market/price-distribution?divisions=` | GET | Price histogram buckets (count per price range) |
| `/market/what-if/coefficients` | GET | Per-feature marginal effect (from the model's `/model-info`) |
| `/market/what-if/sensitivity` | POST | Price curve over one variable (batch `/predict`) |
| `/export/csv` | GET | Dataset as CSV attachment |
| `/export/pdf` | GET | Dataset as PDF attachment |

### Example: sensitivity scan

```bash
curl -X POST http://localhost:8080/market/what-if/sensitivity \
  -H "Content-Type: application/json" \
  -d '{
    "baseline": {"square_footage": 1850, "bedrooms": 3, "bathrooms": 2,
                 "year_built": 1998, "lot_size": 7500,
                 "distance_to_city_center": 5.6, "school_rating": 8.2},
    "variable": "square_footage",
    "min": 1000,
    "max": 3000,
    "steps": 20
  }'
```

## Project Layout

```
src/main/java/com/interview/market/
  MarketAnalysisApplication.java   entry point
  config/RestClientConfig.java     RestClient bean (downstream model service)
  model/Property.java              record: 9 dataset fields
  data/PropertyCsvLoader.java      hand-rolled CSV parser
  data/PropertyRepository.java     in-memory store, loaded at startup
  dto/                             request/response records
  service/                         MarketAnalysisService, WhatIfService, ExportService
  controller/                      PropertiesController, MarketController, ExportController
src/main/resources/
  application.yaml
  data/house-price-dataset.csv     the dataset (copied from Task 1)
```

## Configuration

| Property | Default | Purpose |
|----------|---------|---------|
| `app.model-service.url` | `http://house-price-predict-svc` | Downstream model service |
| `app.model-service.timeout-seconds` | `5` | Per-call timeout |

These are overridden in-cluster via the Helm chart's `modelService.*` values.

## Build & Test

```bash
./mvnw test

# Run locally (requires the downstream service, or point app.model-service.url elsewhere)
./mvnw spring-boot:run
```

## Build & Deploy (k8s)

```bash
docker build -t interview/property-market-analysis-svc:latest .
docker save interview/property-market-analysis-svc:latest -o property-market-analysis-svc.tar
sudo k3s ctr images import property-market-analysis-svc.tar

helm install property-market-analysis-svc ./charts

# Verify
kubectl get pods
kubectl port-forward svc/property-market-analysis-svc 8080:80
curl http://localhost:8080/market/overview
```
