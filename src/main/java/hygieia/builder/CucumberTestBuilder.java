package hygieia.builder;

import com.capitalone.dashboard.model.*;
import com.capitalone.dashboard.request.BuildDataCreateRequest;
import com.capitalone.dashboard.request.TestDataCreateRequest;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hygieia.transformer.CucumberJsonToTestResultTransformer;
import hygieia.utils.HygieiaUtils;
import jenkins.plugins.hygieia.HygieiaPublisher;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class CucumberTestBuilder {
    private static final Logger logger = Logger.getLogger(CucumberTestBuilder.class.getName());
//    private AbstractBuild build;
//    private HygieiaPublisher publisher;
    private Run run;
    private TaskListener listener;
    private String buildId;
    private TestResult testResult;

    private FilePath rootDirectory;
    private String filePattern;
    private String jenkinsName;
    private String testType;
    private String applicationName;
    private String environmentName;
    BuildDataCreateRequest buildDataCreateRequest;

    public CucumberTestBuilder(AbstractBuild build, TaskListener listener, String applicationName, String environmentName, String testType, String filePattern, String directory, String jenkinsName, String buildId) {
        run = build;
        BuildBuilder buildBuilder = new BuildBuilder(build,jenkinsName,listener,true,false);
        this.buildDataCreateRequest = buildBuilder.getBuildData();
        this.buildId = buildId;
        this.listener = listener;
        this.applicationName = applicationName.trim();
        this.environmentName = environmentName.trim();
        this.testType = testType;
        this.filePattern = filePattern.trim();
        this.jenkinsName = jenkinsName.trim();
        rootDirectory = build.getWorkspace().withSuffix(directory);
        buildTestResults();
    }


    public CucumberTestBuilder(Run run, TaskListener listener, BuildStatus buildStatus, FilePath filePath, String applicationName, String environmentName, String testType, String filePattern, String directory, String jenkinsName, String buildId) {
        this.run = run;
        this.buildId = buildId;
        BuildBuilder buildBuilder = new BuildBuilder(run,jenkinsName, listener, buildStatus, false);
        this.buildDataCreateRequest = buildBuilder.getBuildData();
        this.listener = listener;
        this.applicationName = applicationName.trim();
        this.environmentName = environmentName.trim();
        this.testType = testType;
        this.filePattern = filePattern.trim();
        rootDirectory = filePath.withSuffix(directory);
        this.jenkinsName = jenkinsName.trim();
        buildTestResults();
    }

    private void buildTestResults() {

        List<FilePath> testFiles = null;
        try {
            EnvVars envVars = new EnvVars();
            envVars = run.getEnvironment(listener);
            filePattern = envVars.expand(filePattern);
            testFiles = HygieiaUtils.getArtifactFiles(rootDirectory, filePattern, new ArrayList<FilePath>());
            listener.getLogger().println("Hygieia Test Result Publisher - Looking for file pattern '" + filePattern + "' in directory " + rootDirectory.getRemote());
        } catch (IOException e) {
            e.printStackTrace();
            listener.getLogger().println("Hygieia Test Result Publisher" + e.getStackTrace());
        } catch (InterruptedException e) {
            e.printStackTrace();
            listener.getLogger().println("Hygieia Test Result Publisher - InterruptedException on " + e.getStackTrace());
        }
        testResult = buildTestResultObject(getCapabilities(testFiles));
    }

    private List<TestCapability> getCapabilities(List<FilePath> testFiles) {
        List<TestCapability> capabilities = new ArrayList<>();
        JSONParser parser = new JSONParser();
        CucumberJsonToTestResultTransformer cucumberTransformer = new CucumberJsonToTestResultTransformer();
        for (FilePath file : testFiles) {
            try {
                listener.getLogger().println("Hygieia Test Publisher: Processing file: " + file.getRemote());
                JSONArray cucumberJson = (JSONArray) parser.parse(file.readToString());
                TestCapability cap = new TestCapability();
                cap.setType(TestSuiteType.Functional);
                List<TestSuite> testSuites = cucumberTransformer.transformer(cucumberJson);
                cap.setDescription(getCapabilityDescription(file));

                cap.getTestSuites().addAll(testSuites); //add test suites
                long duration = 0;
                int testSuiteSkippedCount = 0, testSuiteSuccessCount = 0, testSuiteFailCount = 0, testSuiteUnknownCount = 0;
                for (TestSuite t : testSuites) {
                    duration += t.getDuration();
                    switch (t.getStatus()) {
                        case Success:
                            testSuiteSuccessCount++;
                            break;
                        case Failure:
                            testSuiteFailCount++;
                            break;
                        case Skipped:
                            testSuiteSkippedCount++;
                            break;
                        default:
                            testSuiteUnknownCount++;
                            break;
                    }
                }
                if (testSuiteFailCount > 0) {
                    cap.setStatus(TestCaseStatus.Failure);
                } else if (testSuiteSkippedCount > 0) {
                    cap.setStatus(TestCaseStatus.Skipped);
                } else if (testSuiteSuccessCount > 0) {
                    cap.setStatus(TestCaseStatus.Success);
                } else {
                    cap.setStatus(TestCaseStatus.Unknown);
                }
                cap.setFailedTestSuiteCount(testSuiteFailCount);
                cap.setSkippedTestSuiteCount(testSuiteSkippedCount);
                cap.setSuccessTestSuiteCount(testSuiteSuccessCount);
                cap.setUnknownStatusTestSuiteCount(testSuiteUnknownCount);
                cap.setTotalTestSuiteCount(testSuites.size());
                cap.setDuration(duration);
                cap.setExecutionId(String.valueOf(buildDataCreateRequest.getNumber()));
                capabilities.add(cap);
            } catch (FileNotFoundException e) {
                listener.getLogger().println("Hygieia Publisher: Test File Not Found: " + file.getRemote());
            } catch (ParseException e) {
                listener.getLogger().println("Hygieia Publisher: Error Parsing File: " + file.getRemote());
            } catch (IOException e) {
                listener.getLogger().println("Hygieia Publisher: Error Reading File: " + file.getName());
            } catch (InterruptedException e) {
                listener.getLogger().println("Hygieia Publisher: Error Reading File: " + file.getName());
            }
        }
        return capabilities;
    }

    private static String getCapabilityDescription(FilePath file) {
        String newFileName = file.getRemote().replace(file.getName(), "");
        boolean isUnix = newFileName.endsWith("/");
        int lastFolderIndex;
        newFileName = newFileName.substring(0, newFileName.length() - 1);
        if (isUnix) {
            lastFolderIndex = newFileName.lastIndexOf("/");
        } else {
            lastFolderIndex = newFileName.lastIndexOf("\\");
        }
        if (lastFolderIndex > 0) {
            return newFileName.substring(lastFolderIndex);
        }
        return newFileName;
    }


    private TestResult buildTestResultObject(List<TestCapability> capabilities) {
        if (!capabilities.isEmpty()) {
            // There are test suites so let's construct a TestResult to encapsulate these results
            TestResult testResult = new TestResult();
            testResult.setType(TestSuiteType.fromString(testType));
            testResult.setDescription(buildDataCreateRequest.getJobName());
            testResult.setExecutionId(String.valueOf(buildDataCreateRequest.getNumber()));
            testResult.setUrl(buildDataCreateRequest.getBuildUrl() + String.valueOf(buildDataCreateRequest.getNumber()) + "/");
            testResult.setDuration(buildDataCreateRequest.getDuration());
            testResult.setEndTime(buildDataCreateRequest.getStartTime() + buildDataCreateRequest.getDuration());
            testResult.setStartTime(buildDataCreateRequest.getStartTime());
            testResult.getTestCapabilities().addAll(capabilities);  //add all capabilities
            testResult.setTotalCount(capabilities.size());
            testResult.setTimestamp(System.currentTimeMillis());
            int testCapabilitySkippedCount = 0, testCapabilitySuccessCount = 0, testCapabilityFailCount = 0;
            int testCapabilityUnknownCount = 0;
            // Calculate counts based on test suites
            for (TestCapability cap : capabilities) {
                switch (cap.getStatus()) {
                    case Success:
                        testCapabilitySuccessCount++;
                        break;
                    case Failure:
                        testCapabilityFailCount++;
                        break;
                    case Skipped:
                        testCapabilitySkippedCount++;
                        break;
                    default:
                        testCapabilityUnknownCount++;
                        break;
                }
            }
            testResult.setSuccessCount(testCapabilitySuccessCount);
            testResult.setFailureCount(testCapabilityFailCount);
            testResult.setSkippedCount(testCapabilitySkippedCount);
            testResult.setUnknownStatusCount(testCapabilityUnknownCount);
            return testResult;
        }
        return null;
    }

    // Helper Methods

    private String getString(JSONObject json, String key) {
        return (String) json.get(key);
    }

    private long getLong(JSONObject json, String key) {
        Object value = json.get(key);
        return value == null ? 0 : (Long) value;
    }

    private JSONArray getJsonArray(JSONObject json, String key) {
        Object array = json.get(key);
        return array == null ? new JSONArray() : (JSONArray) array;
    }

    public TestDataCreateRequest getTestDataCreateRequest() {

        if (testResult != null) {
            TestDataCreateRequest request = new TestDataCreateRequest();
            EnvVars env = null;
            try {
                env = run.getEnvironment(listener);
            } catch (IOException | InterruptedException e) {
                logger.warning("Error getting environment variables");
            }
            if (env != null) {
                request.setServerUrl(env.get("JENKINS_URL"));
            } else {
                String jobPath = "/job" + "/" + buildDataCreateRequest.getJobName() + "/";
                int ind = buildDataCreateRequest.getJobUrl().indexOf(jobPath);
                request.setServerUrl(buildDataCreateRequest.getJobUrl().substring(0, ind));
            }
            request.setTestJobId(buildId);
            request.setType(testResult.getType());
            request.setTestJobName(buildDataCreateRequest.getJobName());
            request.setTestJobUrl(buildDataCreateRequest.getJobUrl());
            request.setTimestamp(testResult.getTimestamp());
            request.setNiceName(jenkinsName);

            request.setDescription(testResult.getDescription());
            request.setDuration(testResult.getDuration());
            request.setEndTime(testResult.getEndTime());
            request.setExecutionId(testResult.getExecutionId());
            request.setFailureCount(testResult.getFailureCount());
            request.setSkippedCount(testResult.getSkippedCount());
            request.setStartTime(testResult.getStartTime());
            request.setSuccessCount(testResult.getSuccessCount());

            request.setTotalCount(testResult.getTotalCount());
            request.setUnknownStatusCount(testResult.getUnknownStatusCount());
            request.getTestCapabilities().addAll(testResult.getTestCapabilities());

            request.setTargetAppName(applicationName);
            request.setTargetEnvName(environmentName);
            return request;
        }
        return null;
    }
}