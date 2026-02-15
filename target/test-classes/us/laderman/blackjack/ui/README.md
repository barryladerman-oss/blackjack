# Blackjack Selenium Tests

This directory contains end-to-end browser-based tests using Selenium WebDriver for the Blackjack application.

## Test Files

### BlackjackSeleniumTest.java
Basic UI interaction tests covering:
- Page loading and element visibility
- Starting a new game
- Game state display (phase, count information, dealer cards)
- Action button enabling/disabling based on game phase
- Hit and stand actions
- Multiple hands support
- Card counting display (true count, running count)
- Suggested bet units
- Complete game flow

**Tests:** 14 test cases

### BlackjackAdvancedSeleniumTest.java
Advanced scenarios and edge cases:
- Multiple game sessions
- Hand count validation
- Rapid successive actions
- Card counting information updates
- Game state JSON validation
- State consistency across actions
- Page refresh recovery
- Form accessibility
- Betting information display
- Maximum hands scenarios

**Tests:** 11 test cases

**Total:** 25 Selenium test cases

## Prerequisites

Required to run Selenium tests:
- Google Chrome browser installed
- ChromeDriver binary available in system PATH or in the project
- Java 17 or higher
- Maven 3.9.5 or higher

## Running Selenium Tests

### Run All Selenium Tests
```bash
# This will start the Spring Boot application on a random port
# and run the Selenium tests against it
mvn test -Dtest=Blackjack*SeleniumTest
```

### Run Specific Test Class
```bash
# Run basic UI tests only
mvn test -Dtest=BlackjackSeleniumTest

# Run advanced tests only
mvn test -Dtest=BlackjackAdvancedSeleniumTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=BlackjackSeleniumTest#testPageLoads
```

### Run All Tests (including Selenium)
```bash
mvn test
```

## Test Coverage

The Selenium tests verify:

### UI Functionality
- ✅ Page loads correctly
- ✅ All required UI elements are present
- ✅ Form controls are accessible and enabled
- ✅ Game state updates are displayed

### Game Actions
- ✅ Starting a new game with configurable hand count
- ✅ Hit action works correctly
- ✅ Stand action works correctly
- ✅ Action buttons enable/disable based on game phase

### Game Information Display
- ✅ Game ID is shown and unique per game
- ✅ Current phase is displayed
- ✅ Dealer upcard is visible
- ✅ Running count is shown
- ✅ True count is calculated and displayed
- ✅ Decks remaining is updated
- ✅ Suggested bet units are provided

### Card Counting Features
- ✅ Card counting information updates on hand actions
- ✅ Betting suggestions change based on true count
- ✅ Multiple games maintain separate count states

### Edge Cases
- ✅ Rapid successive actions are handled
- ✅ Multiple hands in a single game
- ✅ Maximum (5) hands per game
- ✅ Page refresh doesn't break functionality
- ✅ Game state remains consistent

## How the Tests Work

1. **Test Setup:** Each test starts the Spring Boot application on a random port
2. **WebDriver Launch:** ChromeDriver opens a Chrome browser instance
3. **Navigation:** Tests navigate to the application's home page
4. **Interaction:** Tests simulate user actions (clicking buttons, entering text)
5. **Verification:** Tests assert that expected UI changes occur
6. **Cleanup:** Browser is closed after each test

## Test Patterns

### Wait Strategies
Tests use explicitly waits for elements to be:
- Present in the DOM
- Visible on screen
- Enabled for interaction
- Updated with new text content

### State Validation
Tests verify:
- HTML element text content
- Button enabled/disabled states
- JSON data structure validity
- Game state consistency

### Error Handling
Tests gracefully handle:
- Missing elements (fail with clear assertion)
- Timeouts (configurable WebDriverWait with 10-second default)
- Stale element references (wait for element refresh)

## Browser Configuration

Currently configured for **Google Chrome/Chromium**.

To run against a different browser, modify the WebDriver initialization in the `@BeforeEach` method:

### Firefox Example
```java
driver = new FirefoxDriver();
```

### Edge Example
```java
driver = new EdgeDriver();
```

Ensure the corresponding WebDriver is available in your PATH.

## Troubleshooting

### "ChromeDriver not found" Error
**Solution:** Install ChromeDriver matching your Chrome version from [chromedriver.chromium.org](https://chromedriver.chromium.org)

### "Unable to connect to localhost:PORT"
**Solution:** The Spring Boot application failed to start. Check console output for startup errors.

### "Element not found" Errors
**Solution:** The UI element may have changed. Verify the HTML structure and update the By selector or wait condition.

### "Timeout waiting for element"
**Solution:** Increase the wait duration or verify the element is actually rendered on the page.

## CI/CD Integration

To run Selenium tests in CI/CD pipeline:

1. Install Chrome in CI environment (Docker, GitHub Actions, etc.)
2. Run tests with `mvn test -Dtest=*SeleniumTest`
3. Generate test reports from `target/surefire-reports/`

### GitHub Actions Example
```yaml
- name: Install Chrome
  uses: browser-actions/setup-chrome@latest

- name: Run Selenium Tests
  run: mvn test -Dtest=*SeleniumTest
```

## Performance

- **Total execution time:** ~30-60 seconds (all 25 tests)
- **Per test:** ~2-5 seconds average
- Browser startup/teardown adds ~2 seconds per test

## Future Enhancements

Potential improvements:
- [ ] Headless Chrome mode for faster CI execution
- [ ] Screenshot capture on test failure
- [ ] Video recording of test execution
- [ ] Test data builders for complex game scenarios
- [ ] Parallel test execution
- [ ] Performance benchmarking
