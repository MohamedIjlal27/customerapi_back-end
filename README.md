# Customer Management System

A Spring Boot application for managing customer information with support for bulk operations.

## Technologies Used

- Spring Boot 3.2.3
- MariaDB
- JUnit
- Apache POI (for Excel processing)
- Maven

## Prerequisites

- Java 17 or higher
- MariaDB
- Maven

## Setup

1. Create a MariaDB database named `customer_management`:
```sql
CREATE DATABASE customer_management;
```

2. Update the database credentials in `src/main/resources/application.properties` if needed.

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

## API Endpoints

### Customer Management

- `POST /api/customers` - Create a new customer
- `PUT /api/customers/{id}` - Update an existing customer
- `GET /api/customers/{id}` - Get a customer by ID
- `GET /api/customers` - Get all customers
- `DELETE /api/customers/{id}` - Delete a customer

### Bulk Operations

- `POST /api/customers/bulk-create` - Create multiple customers from Excel file
- `POST /api/customers/bulk-update` - Update multiple customers from Excel file

## Excel File Format

For bulk operations, the Excel file should have the following columns:
1. Name (mandatory)
2. Date of Birth (mandatory, format: YYYY-MM-DD)
3. NIC Number (mandatory, unique)

## Data Model

### Customer
- Name (mandatory)
- Date of Birth (mandatory)
- NIC Number (mandatory, unique)
- Mobile Numbers (optional, multiple)
- Addresses (optional, multiple)
- Family Members (optional, multiple)

### Address
- Address Line 1
- Address Line 2
- City
- Country

### City
- Name
- Country (reference)

### Country
- Name
- Code

## Testing

Run the tests using:
```bash
mvn test
```

## Performance Considerations

- The application uses JPA's lazy loading for related entities
- Bulk operations are processed in chunks to handle large files
- Database indexes are created for frequently queried fields
- Transaction management is implemented for data consistency

## API Documentation

Once the application is running, you can access the Swagger UI for interactive API documentation at:

- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) 