package com.example.api.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

public class TestListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        Reporter.log("Starting test: " + result.getName(), true);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        Reporter.log("Test passed: " + result.getName(), true);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Reporter.log("Test failed: " + result.getName() + "\n" + result.getThrowable().getMessage(), true);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Reporter.log("Test skipped: " + result.getName(), true);
    }

    @Override
    public void onStart(ITestContext context) {
        Reporter.log("Test Suite " + context.getName() + " started", true);
    }

    @Override
    public void onFinish(ITestContext context) {
        Reporter.log("Test Suite " + context.getName() + " finished", true);
    }
}
