package todoapp.listeners;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AllureReportListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(AllureReportListener.class);

    @Override
    public void onStart(ITestContext context) {
        logger.info("Starting test suite: {}", context.getName());
        addEnvironmentDetails();
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Starting test: {}.{}",
            result.getTestClass().getName(), result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed: {}", result.getName());
        addTestExecutionDetails(result, "PASSED");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test failed: {}", result.getName());
        addTestExecutionDetails(result, "FAILED");

        // Add exception details
        if (result.getThrowable() != null) {
            Allure.addAttachment("Exception Details",
                result.getThrowable().toString());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped: {}", result.getName());
        addTestExecutionDetails(result, "SKIPPED");
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("Finished test suite: {}", context.getName());

        // Add test suite summary
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        int total = passed + failed + skipped;

        Allure.addAttachment("Test Suite Summary", String.format(
            "Total Tests: %d\n" +
                "Passed: %d (%.2f%%)\n" +
                "Failed: %d (%.2f%%)\n" +
                "Skipped: %d (%.2f%%)",
            total,
            passed, (double)passed/total*100,
            failed, (double)failed/total*100,
            skipped, (double)skipped/total*100
        ));
    }

    private void addEnvironmentDetails() {
        Allure.addAttachment("Environment Info", String.format(
            "Java Version: %s\n" +
                "OS: %s %s\n" +
                "User: %s\n" +
                "Timestamp: %s",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("user.name"),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        ));
    }

    private void addTestExecutionDetails(ITestResult result, String status) {
        long durationMs = result.getEndMillis() - result.getStartMillis();

        Allure.addAttachment("Test Execution Details", String.format(
            "Status: %s\n" +
                "Duration: %d ms\n" +
                "Thread: %s",
            status,
            durationMs,
            Thread.currentThread().getName()
        ));
    }
}