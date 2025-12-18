# Ticari CRM - Pre-Accounting / ERP Desktop Application

## Overview
A comprehensive Pre-Accounting / ERP Desktop Application built with Spring Boot 3.x and JavaFX 21. This application manages customers, products, invoices, quotes, stock movements, and financial transactions.

## Tech Stack
- **Java**: 21
- **Framework**: Spring Boot 3.2.1
- **UI**: JavaFX 21.0.6
- **Database**: Microsoft SQL Server Express
- **ORM**: Spring Data JPA (Hibernate)
- **Build Tool**: Maven
- **Additional**: Lombok for boilerplate reduction

## Project Structure
```
com.ticari
├── entity/             # JPA Entity classes (19 entities)
├── repository/         # Spring Data JPA Repositories
├── service/            # Business logic layer
├── controller/         # JavaFX Controllers with Spring DI
├── config/             # Spring-JavaFX bridge configuration
├── enums/              # Enum classes for status and types
└── dto/                # Data Transfer Objects (future use)
```

## Database Schema
The application manages 19 tables:
- **Cariler**: Customer/Supplier accounts
- **Personeller**: Employee management
- **Kategoriler**: Product categories
- **Urunler**: Product/inventory management
- **Hizmetler**: Services
- **Teklifler & TeklifDetaylari**: Quotes and quote details
- **Siparisler**: Orders
- **Faturalar**: Invoices
- **StokGirisCikis**: Stock movements
- **FaturaHizmetKalemleri**: Invoice service items
- **KasaBanka**: Cash/Bank accounts
- **CekSenetler**: Checks and promissory notes
- **FinansHareketleri**: Financial transactions
- **Atiklar & AtikNedenleri**: Waste management
- **ParaBirimleri & Kurlar**: Currency and exchange rates

## Setup Instructions

### Prerequisites
1. Java 21 or later installed
2. Maven installed
3. Microsoft SQL Server Express installed and running

### Database Setup
1. Create a database named `TicariDB` in SQL Server
2. Run the SQL schema script to create tables (provided in project documentation)
3. Update database credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TicariDB;encrypt=true;trustServerCertificate=true
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   ```

### Building and Running

#### Option 1: Using Maven
```bash
# Clean and compile
mvn clean compile

# Run the application
mvn javafx:run
```

#### Option 2: Using Maven Spring Boot Plugin
```bash
# Package the application
mvn clean package

# Run the JAR
java -jar target/javaCrm-1.0-SNAPSHOT.jar
```

## Features

### Implemented
1. **Entity Layer**: All 19 entities with proper JPA annotations and relationships
2. **Repository Layer**: Spring Data JPA repositories with custom queries
3. **Service Layer**: Business logic for core domains:
   - CariService: Customer/Supplier management
   - UrunService: Product/inventory management
   - FaturaService: Invoice management with automatic stock updates
   - StokService: Stock movement tracking
   - TeklifService: Quote management
   - KasaBankaService: Cash/Bank account management
   - FinansService: Financial transaction management

4. **Controller Layer**: JavaFX controllers with Spring DI:
   - CariController: Customer list and management
   - UrunController: Product list and inventory tracking

5. **Spring-JavaFX Integration**:
   - JavaFXApplication: Bridges Spring and JavaFX lifecycles
   - StageInitializer: Initializes the primary stage
   - SpringFXMLLoader: Enables Spring dependency injection in FXML controllers

### Business Logic Highlights
- **Automatic Stock Updates**: When invoices are saved, stock levels are automatically updated based on stock movements
- **Automatic Balance Updates**: Customer/supplier balances are updated when invoices or financial transactions are created
- **Search Functionality**: Real-time search in customer and product lists
- **Low Stock Alerts**: Visual indicators for products with low stock levels

## FXML Files
- `cariler.fxml`: Customer/Supplier management screen
- `urun.fxml`: Product/inventory management screen
- `faturalar.fxml`: Invoice list screen
- `yenifatura.fxml`: New invoice creation screen
- `teklifler.fxml`: Quotes management screen
- `kasa.fxml`: Cash/Bank management screen

## Configuration
Key configuration in `application.properties`:
- Database connection settings
- JPA/Hibernate settings (DDL auto-update, SQL logging)
- Application name and server port

## Development Notes

### Entity Relationships
- `@OneToMany` and `@ManyToOne` relationships properly configured
- Cascade operations defined where appropriate
- Lazy loading enabled for performance

### Enum Usage
Status and type fields are implemented as Java enums:
- `CariTuru`: ALICI, SATICI, DIGER
- `FaturaTuru`: ALIS, SATIS, GIDER
- `StokIslemTuru`: GIRIS, CIKIS
- `TeklifDurumu`: BEKLIYOR, ONAYLANDI, REDDEDILDI
- And more...

### Spring Integration
- Controllers are Spring-managed beans (`@Controller`)
- Services use `@Service` and `@Transactional`
- Repositories extend `JpaRepository`
- Constructor-based dependency injection with Lombok's `@RequiredArgsConstructor`

## Future Enhancements
- Complete remaining FXML controllers (faturalar, yenifatura, teklifler, kasa)
- Add form dialogs for create/edit operations
- Implement reporting functionality
- Add user authentication and authorization
- Implement PDF export for invoices and reports
- Add dashboard with key metrics
- Multi-currency support integration

## Troubleshooting

### Common Issues
1. **Database Connection Failed**: Verify SQL Server is running and credentials are correct
2. **JavaFX Runtime Error**: Ensure Java 21 with JavaFX modules is installed
3. **Compilation Errors**: Run `mvn clean install` to refresh dependencies

### Logging
Application logs are configured in `application.properties`. Check console output for:
- SQL queries (when `spring.jpa.show-sql=true`)
- Hibernate SQL parameter binding
- Application-level logs

## License
[Specify your license here]

## Contributors
[Specify contributors here]
