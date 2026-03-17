# Loan Application Service

A Spring Boot REST service that evaluates loan applications, generates a single offer for the requested tenure when eligible, and stores every decision for audit in H2.

## Tech Stack

- Java 17
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- Jakarta Validation
- H2 Database
- Maven 3.9+ or the included Maven Wrapper (`mvnw`, `mvnw.cmd`)
- JUnit 5 / Spring Boot Test

## Tested With

- Java 17.0.18
- Maven 3.9.14
- H2 2.4.240

## Features

- `POST /applications` endpoint for loan evaluation
- Input validation with meaningful `400 Bad Request` responses
- Eligibility checks for credit score, age plus tenure, and EMI affordability
- Risk band classification based on credit score
- Final interest rate calculation using risk, employment, and loan-size premiums
- Single-offer generation using the requested tenure only
- Audit persistence for approved and rejected applications
- H2 web console for quick inspection
- Unit and controller tests covering approval, rejection, validation, and route handling

## Business Rules Implemented

### Validation

- Age: `21` to `60`
- Credit score: `300` to `900`
- Loan amount: `10,000` to `50,00,000`
- Tenure: `6` to `360` months
- Monthly income: greater than `0`

### Eligibility

- Reject if credit score is below `600`
- Reject if `age + tenure` exceeds `65` years
- Reject if EMI at base `12%` interest exceeds `60%` of monthly income

### Risk Bands

- `750+` -> `LOW`
- `650-749` -> `MEDIUM`
- `600-649` -> `HIGH`

### Final Interest Rate

`Base Rate + Risk Premium + Employment Premium + Loan Size Premium`

- Base rate: `12.00%`
- Risk premium:
  - `LOW` -> `0.00%`
  - `MEDIUM` -> `1.50%`
  - `HIGH` -> `3.00%`
- Employment premium:
  - `SALARIED` -> `0.00%`
  - `SELF_EMPLOYED` -> `1.00%`
- Loan size premium:
  - `Loan > 10,00,000` -> `0.50%`
  - Otherwise -> `0.00%`

### Offer Rule

- Generate only one offer for the requested tenure
- Reject the application if the final-offer EMI exceeds `50%` of monthly income

## Prerequisites

- Java 17 installed
- `JAVA_HOME` pointing to your JDK

You can use either a local Maven installation or the Maven Wrapper included in the repo.

## Run The Application

### Windows PowerShell

```powershell
$env:JAVA_HOME="C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

mvn spring-boot:run
```

The application starts on:

- `http://localhost:8080`

If port `8080` is already in use, run on a different port:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

### macOS / Linux

```bash
export JAVA_HOME=/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"

mvn spring-boot:run
```

## Run Tests

### Windows PowerShell

```powershell
$env:JAVA_HOME="C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

mvn test
```

### macOS / Linux

```bash
export JAVA_HOME=/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"

mvn test
```

### Optional Maven Wrapper Commands

If you prefer the wrapper, you can use:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```

If the wrapper throws an environment-specific error in Windows PowerShell, use your local Maven installation (`mvn` or `mvn.cmd`) instead.

## API

### Endpoint

- `POST /applications`

### Request Example

```json
{
  "applicant": {
    "name": "Rohit Kumar",
    "age": 30,
    "monthlyIncome": 50000,
    "employmentType": "SALARIED",
    "creditScore": 720
  },
  "loan": {
    "amount": 500000,
    "tenureMonths": 36,
    "purpose": "PERSONAL"
  }
}
```

### Approved Response Example

```json
{
  "applicationId": "7351d119-d035-4148-be84-8aba6abc0ac4",
  "status": "APPROVED",
  "riskBand": "MEDIUM",
  "offer": {
    "interestRate": 13.5,
    "tenureMonths": 36,
    "emi": 16967.64,
    "totalPayable": 610835.04
  }
}
```

### Rejected Response Example

```json
{
  "applicationId": "860e1631-b9b3-4e11-9789-201d136e972e",
  "status": "REJECTED",
  "riskBand": null,
  "rejectionReasons": [
    "EMI_EXCEEDS_50_PERCENT_OFFER_LIMIT"
  ]
}
```

### Validation Error Example

```json
{
  "timestamp": "2026-03-17T11:12:14.629304700Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    "applicant.name: name is required",
    "applicant.age: age must be between 21 and 60",
    "applicant.monthlyIncome: monthlyIncome must be greater than 0",
    "applicant.creditScore: creditScore must be between 300 and 900",
    "loan.amount: amount must be between 10000 and 5000000",
    "loan.tenureMonths: tenureMonths must be between 6 and 360"
  ]
}
```

## Quick Test Commands

### PowerShell Approved Case

```powershell
$body = @{
  applicant = @{
    name = "Rohit Kumar"
    age = 30
    monthlyIncome = 50000
    employmentType = "SALARIED"
    creditScore = 720
  }
  loan = @{
    amount = 500000
    tenureMonths = 36
    purpose = "PERSONAL"
  }
} | ConvertTo-Json -Depth 5

Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/applications" `
  -ContentType "application/json" `
  -Body $body
```

### PowerShell Rejected Case

```powershell
$body = @{
  applicant = @{
    name = "Rohit Kumar"
    age = 30
    monthlyIncome = 33000
    employmentType = "SALARIED"
    creditScore = 720
  }
  loan = @{
    amount = 500000
    tenureMonths = 36
    purpose = "PERSONAL"
  }
} | ConvertTo-Json -Depth 5

Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/applications" `
  -ContentType "application/json" `
  -Body $body
```

### PowerShell Validation Error Case

```powershell
$body = @{
  applicant = @{
    name = ""
    age = 20
    monthlyIncome = 0
    employmentType = "SALARIED"
    creditScore = 250
  }
  loan = @{
    amount = 9999
    tenureMonths = 3
    purpose = "PERSONAL"
  }
} | ConvertTo-Json -Depth 5

Invoke-WebRequest -Method POST `
  -Uri "http://localhost:8080/applications" `
  -ContentType "application/json" `
  -Body $body
```

## H2 Console

The H2 console is available when the app is running:

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/loan-application-db;AUTO_SERVER=TRUE`
- Username: `sa`
- Password: leave blank

Useful queries:

```sql
select * from loan_application_audit;
```

```sql
select * from loan_application_rejection_reason;
```

## Project Structure

```text
src/main/java/com/takehome/loanservice
|- api
|- domain
|- exception
|- repository
|- service
```

## Notes

- Financial calculations use `BigDecimal` with scale `2` and `RoundingMode.HALF_UP`
- All decisions are persisted for audit
- Rejected responses intentionally return `riskBand: null` to match the assignment format
- Additional implementation notes are available in `DEVELOPMENT_NOTES.md`

## Git History

The repository keeps incremental commits as requested in the assignment instead of a single bulk commit.
