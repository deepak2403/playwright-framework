# Playwright + TestNG Automation Framework

## Overview

A hybrid automation framework combining Playwright (UI) and REST Assured (API) with TestNG as the test runner. Built to test Amazon.in e2e flows (search → filter → cart → checkout) across multiple user accounts in parallel, alongside API validation against automationexercise.com.

---

## Project Structure

```
src/
├── main/java/
│   ├── api/
│   │   ├── clients/
│   │   │   ├── AuthApiClient.java        # POST /api/verifyLogin (raw response)
│   │   │   └── ProductApiClient.java     # GET/POST product endpoints
│   │   ├── core/
│   │   │   └── ApiHelper.java            # Base REST Assured config
│   │   └── models/
│   │       └── ProductResponse.java      # Response POJO (retained for future use)
│   ├── base/
│   │   └── BaseTest.java                 # Thread-local browser lifecycle, session management
│   ├── core/
│   │   ├── SessionManager.java           # storageState save/load, per-user session paths
│   │   └── UserConfig.java               # Per-instance config carrier for @Factory runs
│   ├── listeners/
│   │   └── TestListener.java             # Allure + logging hooks
│   ├── pages/
│   │   ├── HomePage.java                 # Open, login, search
│   │   ├── ProductPage.java              # Filters, add to cart
│   │   └── CheckoutPage.java             # Checkout flow
│   └── utils/
│       ├── ConfigReader.java             # config.yml reader
│       └── WaitHelper.java               # Playwright wait utilities
│
├── test/java/
│   └── tests/
│       ├── ApiTest.java                  # API layer tests (automationexercise.com)
│       ├── CheckoutTest.java             # Checkout continuation from EcommerceTest
│       ├── EcommerceTest.java            # Search → filter → add to cart
│       └── EcommerceTestFactory.java     # @Factory: creates multi-user instances
│
└── test/resources/
    ├── config.yml                        # App URL, credentials, device config
    ├── session/
    │   └── storageState_*.json           # Per-user session files (git-ignored)
    └── suites/
        ├── e2e-suite.xml                 # E2E parallel run
        └── api-suite.xml                 # API-only run
```

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 11+ |
| Maven | 3.6+ |
| Chrome | Latest stable |
| Playwright Java | 1.x |

---

## Setup

**1. Clone and install dependencies**
```bash
mvn clean install -DskipTests
```

**2. Install Playwright browsers**
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

**3. Configure `config.yml`**
```yaml
applicationUrl: https://www.amazon.in
laptopBrand: MSI
device: Laptop 5050

creds:
  username: your_email@gmail.com
  password: YourPassword

deviceConfig:
  ramSize: "16"
  ramType: DDR5
```

**4. Configure test accounts in `EcommerceTestFactory.java`**
```java
new EcommerceTest(new UserConfig(
    "user1@gmail.com", "Password1",
    "MSI Laptop 5050", "MSI", "5050", "16", "DDR5"
)),
new EcommerceTest(new UserConfig(
    "user2@gmail.com", "Password2",
    "MSI Laptop 5050", "MSI", "5050", "16", "DDR5"
))
```

> **Security note:** Never commit `EcommerceTestFactory.java` with real credentials to Git. Add it to `.gitignore` or use environment variables.

---

## Running Tests

**E2E suite (parallel, multi-user)**
```bash
mvn test -Dsuite=e2e
```

**API suite only**
```bash
mvn test -Dsuite=api
```

**Single test class**
```bash
mvn test -Dtest=EcommerceTest
```

---

## Architecture

### Thread-Local Browser Model

Each parallel test instance gets its own `Playwright` + `Browser` created on its own thread. This is required because Playwright's browser object is bound to the thread that created it — sharing a static browser across threads causes `Cannot find object to call __adopt__` errors.

```
Thread 1 (Instance 1)          Thread 2 (Instance 2)
─────────────────────          ─────────────────────
Playwright.create()            Playwright.create()
chromium.launch()              chromium.launch()
performSessionLogin()          performSessionLogin()
newContext(storageState1)      newContext(storageState2)
classPage → tests run          classPage → tests run
browser.close()                browser.close()
playwright.close()             playwright.close()
```

Key fields in `BaseTest`:
```java
private static final ThreadLocal<Playwright> tlPlaywright = new ThreadLocal<>();
private static final ThreadLocal<Browser>    tlBrowser    = new ThreadLocal<>();
private static final ThreadLocal<Page>       tlClassPage  = new ThreadLocal<>();
private static final ThreadLocal<BrowserContext> tlClassCtx = new ThreadLocal<>();
```

### Session Management

On first run, each user instance logs in via UI and saves a `storageState` JSON file keyed to their email. Subsequent runs skip login and load the saved session directly.

Session files are stored at:
```
src/test/resources/session/storageState_user1_gmail_com.json
src/test/resources/session/storageState_user2_gmail_com.json
```

To force a fresh login, delete the relevant session file and re-run.

### Page Scoping

Two scopes exist to support both chained and isolated tests:

| Scope | ThreadLocal | Used by | Lifecycle |
|---|---|---|---|
| Class-scoped | `tlClassPage` | `EcommerceTest`, `CheckoutTest` | Lives for the entire test class — preserves navigation state across `dependsOnMethods` chains |
| Method-scoped | `tlPage` | Smoke tests, isolated tests | Fresh page per test method — call `setUpMethodPage()` in subclass `@BeforeMethod` |

### @Factory Pattern

`EcommerceTestFactory` creates multiple `EcommerceTest` instances, each carrying a `UserConfig`. TestNG runs them as independent parallel instances.

```java
@Factory
public Object[] createInstances() {
    return new Object[] {
        new EcommerceTest(new UserConfig(...)),
        new EcommerceTest(new UserConfig(...))
    };
}
```

`EcommerceTest` overrides `getUserConfig()` from `BaseTest` to supply its instance config. `BaseTest` uses this hook to resolve the correct session path and perform per-user login.

Single-user runs (direct class execution, no factory) are unaffected — `getUserConfig()` returns `null` and all methods fall back to `config.yml`.

---

## Key Components

### `UserConfig`

Carries all per-instance data for factory runs. No behaviour — purely a data carrier.

| Field | Used in | Purpose |
|---|---|---|
| `email` | `BaseTest.classSetUp()` | Resolves per-user session file path |
| `email` + `password` | `BaseTest.performSessionLogin()` | Logs in as the correct user |
| `searchTerm` | `EcommerceTest.navigateAndSearchProduct()` | What to search on Amazon |
| `brand` | `EcommerceTest.applyProductFilters()`, `addProductToCart()` | Brand filter + card filter |
| `secondaryKeyword` | `EcommerceTest.addProductToCart()` | Narrows product cards (e.g. `"5050"`, `"Intel"`) — optional, pass empty string to skip |
| `ramSize` | `EcommerceTest.applyProductFilters()` | RAM size filter (e.g. `"16"`) |
| `ramType` | `EcommerceTest.applyProductFilters()` | RAM type filter (e.g. `"DDR5"`) |

### `ProductPage.addToCart(String brand, String secondaryKeyword)`

Filters product cards by brand and optional secondary keyword, then selects the first card where rating ≥ 3.5 and price ≤ ₹1,10,000. Opens the product in a new tab and clicks Add to Cart. Returns the new tab's `Page` for checkout chaining.

Pass an empty string for `secondaryKeyword` to filter by brand alone.

### `SessionManager`

| Method | Purpose |
|---|---|
| `getSessionPath()` | Default single-user session path |
| `getSessionPath(String email)` | Per-user session path derived from email |
| `saveSession(context)` | Save to default path |
| `saveSession(context, path)` | Save to explicit path (used by factory) |
| `sessionExists()` | Check if default session file exists |

---

## Test Groups and Suites

### E2E Suite (`e2e-suite.xml`)

```xml
<suite name="E2E Suite" parallel="instances" thread-count="2">
    <test name="E2E Checkout">
        <classes>
            <class name="tests.EcommerceTestFactory"/>
        </classes>
    </test>
</suite>
```

`parallel="instances"` runs each factory-produced instance on its own thread. `thread-count` should match the number of user instances in the factory.

### API Suite (`api-suite.xml`)

Runs `ApiTest` against automationexercise.com. No browser involved — REST Assured only.

| Test | Group | What it validates |
|---|---|---|
| `verifyLoginWithInvalidCredentials` | regression | POST /api/verifyLogin returns 404 in body for bad credentials |
| `verifyProductsListStatusCode` | smoke | GET /api/productsList returns HTTP 200 |
| `verifySearchProductWrongMethod` | regression | GET on POST-only endpoint returns 405 in body |
| `verifySearchWithEmptyKeyword` | regression | Empty keyword search returns HTTP 200 |
| `verifyProductsApiResponseTime` | regression | Products list responds within 3000ms |

---

## Allure Reporting

**Generate and open report after a run:**
```bash
mvn allure:report
mvn allure:serve
```

Tests are annotated with `@Epic`, `@Feature`, `@Story`, `@Severity`, and `@Description` for full hierarchy in the report.

---

## Common Issues

### `Cannot find object to call __adopt__`

Playwright browser shared across threads. Ensure `browser` and `playwright` are `ThreadLocal` — not `static` — in `BaseTest`. Each thread must create its own `Playwright.create()` and `chromium.launch()`.

### `this.page is null` in page classes

`classSetUp()` failed before setting `tlClassPage`. Check the log for the preceding error — usually a session or browser setup failure. Fix the root cause; the null page is a consequence, not the source.

### Session expired or corrupted

Delete the affected session file from `src/test/resources/session/` and re-run. The framework will perform a fresh login and save a new session.

### Filter locator not matching

Amazon occasionally adds whitespace to filter label text. `applyFilters()` uses `normalize-space()` in XPath to handle this. If filters still fail, open the page manually, inspect the filter label element, and verify the exact text matches the value in `UserConfig`.

### No eligible product found

`addToCart()` throws if no card passes the rating ≥ 3.5 and price ≤ ₹1,10,000 threshold. Either the filter criteria are too strict for current listings, or `secondaryKeyword` doesn't appear in any card text. Check Amazon manually with the same search + filters to confirm eligible products exist.

---

## gitignore Recommendations

```gitignore
# Session files contain auth cookies — never commit
src/test/resources/session/

# Factory file contains plaintext credentials
src/test/java/tests/EcommerceTestFactory.java

# Build output
target/
allure-results/
allure-report/
```
