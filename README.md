# Playwright Java E2E Test Automation Framework

![CI](https://img.shields.io/badge/CI-Jenkins-blue?logo=jenkins)
![Java](https://img.shields.io/badge/Java-11-orange?logo=java)
![Playwright](https://img.shields.io/badge/Playwright-Java-green)
![TestNG](https://img.shields.io/badge/TestNG-Framework-red)
![Allure](https://img.shields.io/badge/Allure-Reports-yellow)

A production-ready end-to-end test automation framework built with **Playwright + Java + TestNG**, integrated with **Jenkins CI/CD** and **Allure reporting**.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Browser Automation | Playwright (Java) |
| Test Framework | TestNG |
| Build Tool | Maven |
| API Testing | REST Assured |
| Reporting | Allure Reports |
| CI/CD | Jenkins (Declarative Pipeline) |
| Version Control | GitHub (webhook-triggered builds) |

---

## Framework Architecture

```
src/
├── test/
│   ├── java/
│   │   ├── base/          # BaseTest — ThreadLocal browser/context lifecycle
│   │   ├── core/          # SessionManager, UserConfig, ConfigReader
│   │   ├── pages/         # Page Object Model (HomePage, ProductPage, CheckoutPage)
│   │   ├── tests/         # CheckoutTest, ApiTest
│   │   ├── utils/         # WaitHelper
│   │   └── listeners/     # TestListener — screenshot on failure
│   └── resources/
│       ├── config.yml     # Test config (injected via Jenkins credentials)
│       ├── session/       # Saved browser session (storageState.json)
│       └── testng/        # Suite XML files (api.xml, e2e.xml)
```

---

## Test Suites

| Suite | Command | Coverage |
|---|---|---|
| API Tests | `mvn test -Dsuite=api` | REST API validation via REST Assured |
| E2E Tests | `mvn test -Dsuite=e2e` | Search → Filter → Add to Cart → Checkout |

---

## CI/CD Pipeline (Jenkins)

The pipeline is triggered automatically on every GitHub push via webhook.

**Pipeline stages:**

```
Checkout → Build → Install Playwright Browsers → Run API Tests → Run E2E Tests → Allure Report
```

**Key CI/CD design decisions:**
- Secrets (credentials, session state) injected via Jenkins Credentials — never stored in Git
- Browser session saved locally once and injected as a Jenkins secret file to avoid re-authentication in CI
- Headless mode enabled automatically via `-Dplaywright.headless=true`
- Allure results published as a Jenkins build artifact after every run

---

## Running Locally

**Prerequisites:** Java 11, Maven, Chrome

```bash
# Clone the repo
git clone https://github.com/deepak2403/playwright-framework.git

# Run API tests
mvn test -Dsuite=api

# Run E2E tests
mvn test -Dsuite=e2e
```

> **Note:** A valid `config.yml` and `session/storageState.json` are required.
> These are not committed to Git — generate via local login or contact the repo owner.

---

## Allure Report

After a test run, generate and open the report locally:

```bash
mvn allure:report
mvn allure:serve
```

Or view the latest report directly from the Jenkins build artifacts.

---

## Key Design Patterns

- **Page Object Model (POM)** — clean separation of locators and actions
- **ThreadLocal browser instances** — parallel-safe execution
- **Session reuse** — login once, reuse `storageState.json` across tests
- **@Factory pattern** — multi-user test runs with `UserConfig` data carrier
- **Screenshot on failure** — captured automatically by `TestListener`