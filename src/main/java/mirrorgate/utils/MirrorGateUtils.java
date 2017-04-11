package mirrorgate.utils;

import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;

public class MirrorGateUtils {
    private static final Logger LOGGER = Logger.getLogger(MirrorGateUtils.class.getName());
    public static final String APPLICATION_JSON_VALUE = "application/json";

    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new CustomObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

    public static Object convertJsonToObject(String json, Class thisClass) throws IOException {
        ObjectMapper mapper = new CustomObjectMapper();
        return mapper.readValue(json, thisClass);
    }

    public static String getBuildUrl(Run<?, ?> run) {
        return run.getParent().getAbsoluteUrl() + String.valueOf(run.getNumber()) + "/";
    }

    public static String getBuildNumber(Run<?, ?> run) {
        return String.valueOf(run.getNumber());
    }

    public static String getJobUrl(Run<?, ?> run) {
        return run.getParent().getAbsoluteUrl();
    }

    public static String getJobName(Run<?, ?> run) {
        return run.getParent().getDisplayName();
    }

    public static String getInstanceUrl(Run<?, ?> run, TaskListener listener) {
        String envValue = getEnvironmentVariable(run, listener, "JENKINS_URL");

        if (envValue != null) {
            return envValue;
        } else {
            String jobPath = "/job" + "/" + run.getParent().getName() + "/";
            int ind = run.getParent().getAbsoluteUrl().indexOf(jobPath);
            return run.getParent().getAbsoluteUrl().substring(0, ind);
        }
    }
    
    public static EnvVars getEnvironment(Run<?, ?> run, TaskListener listener) {
        EnvVars env = null;
        try {
            env = run.getEnvironment(listener);
        } catch (IOException | InterruptedException e) {
            LOGGER.warning("Error getting environment variables");
        }
        return env;
    }

    public static String getEnvironmentVariable(Run<?, ?> run, TaskListener listener, String key) {
        EnvVars env = getEnvironment(run, listener);
        if (env != null) {
            return env.get(key);
        } else {
            return null;
        }
    }

    public static int getSafePositiveInteger(String value, int defaultValue) {
        int returnValue = defaultValue;
        if (value != null) {
            try {
                returnValue = Integer.parseInt(value.trim());
                if (returnValue < 0) {
                    returnValue = defaultValue;
                }
            } catch (java.lang.NumberFormatException nfe) {
                //do nothing. will return default at the end.
            }
        }
        return returnValue;
    }

}