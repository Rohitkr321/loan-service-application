# Loan Application Service

Spring Boot REST API for evaluating loan applications and storing decisions in H2.

## Versions

- Java 17
- Spring Boot 4.0.3
- Maven 3.9+
- H2 Database

## Run

Use Windows PowerShell from the project root:

```powershell
$env:JAVA_HOME="C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

mvn spring-boot:run
```

App URL:

- `http://localhost:8080`

If `8080` is busy:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

## Test

```powershell
$env:JAVA_HOME="C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

mvn test
```

## API

Endpoint:

- `POST /applications`

Sample request:

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

Sample approved response:

```json
{
  "applicationId": "UUID",
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

## H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/loan-application-db;AUTO_SERVER=TRUE`
- Username: `sa`
- Password: leave blank

Useful queries:

```sql
select * from loan_application_audit;
select * from loan_application_rejection_reason;
```

## Notes

- Financial calculations use `BigDecimal`
- Validation errors return `400 Bad Request`
- Decisions are saved in H2 for audit
