# Development Notes

## Overall Approach

I built the assignment as a layered Spring Boot REST service with:

- `api` for request/response contracts and the `POST /applications` controller
- `service` for business rules, EMI calculation, and orchestration
- `domain` for business enums and immutable records
- `repository` for audit persistence using Spring Data JPA and H2

Each application is evaluated once, a single decision is produced for the requested tenure, and the result is stored in an audit table.

## Key Design Decisions

- Used `BigDecimal` throughout the financial path with `scale = 2` and `RoundingMode.HALF_UP`.
- Split business logic into small focused services:
  - `EmiCalculator` for the EMI formula
  - `RiskBandClassifier` for score banding
  - `LoanDecisionEngine` for eligibility, pricing, and offer generation
- Stored audit data in a flattened relational structure so the submission can be run and inspected quickly with H2.
- Returned a single response shape for both approved and rejected outcomes while keeping `riskBand` explicitly `null` for rejected applications, as shown in the assignment.
- Added targeted tests for EMI, risk band classification, eligibility logic, and end-to-end controller behavior.

## Trade-offs Considered

- I did not add a retrieval endpoint for past applications because the assignment only asks for creation/evaluation plus audit storage.
- I used H2 instead of a heavier external database to keep the submission self-contained and easy to run.
- I kept the persistence model flat rather than storing raw request/response JSON. That makes basic auditing and SQL inspection easy, but is less flexible than a hybrid audit design.

## Assumptions Made

- Valid application evaluations return `200 OK`. The document explicitly calls out `400` for invalid input but does not prescribe a success status code.
- The age + tenure rule is evaluated in months (`age * 12 + tenureMonths > 65 * 12`) so partial-year tenures are handled accurately.
- The `EMI > 60%` eligibility rule uses the base 12% rate from the eligibility section.
- The `EMI > 50%` rejection during offer generation uses the final interest rate after all premiums are applied.
- Rejected responses intentionally expose `riskBand = null` even when the credit score would otherwise map to a band, because that is the format shown in the assignment.
- Spring Initializr metadata labeled the generated version as `4.0.3.RELEASE`, but the published Maven artifact is `4.0.3`, so the parent version was corrected to the resolvable release.

## Improvements With More Time

- Add a `GET /applications/{id}` endpoint for audit retrieval.
- Add structured error codes alongside human-readable validation messages.
- Add Testcontainers-backed persistence tests for closer-to-production verification.
- Add API documentation via OpenAPI/Swagger.
- Add profiles for local/dev/test separation and quieter logging defaults.
