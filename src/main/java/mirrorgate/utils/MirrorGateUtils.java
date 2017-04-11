package mirrorgate.utils;

import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
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

    public static List<FilePath> getArtifactFiles(FilePath rootDirectory, String pattern, List<FilePath> results) throws IOException, InterruptedException {
        FileFilter filter = new WildcardFileFilter(pattern.replace("**", "*"), IOCase.SYSTEM);
        List<FilePath> temp = rootDirectory.list(filter);
        if (!CollectionUtils.isEmpty(temp)) {
            results.addAll(temp);
        }

        temp = rootDirectory.list();
        if (!CollectionUtils.isEmpty(temp)) {
            for (FilePath currentItem : rootDirectory.list()) {
                if (currentItem.isDirectory()) {
                    getArtifactFiles(currentItem, pattern, results);
                }
            }
        }
        return results;
    }

    public static String getFileNameMinusVersion(FilePath file, String version) {
        String ext = FilenameUtils.getExtension(file.getName());
        if ("".equals(version)) return file.getName();

        int vIndex = file.getName().indexOf(version);
        if (vIndex <= 0) return file.getName();
        if ((file.getName().charAt(vIndex - 1) == '-') || (file.getName().charAt(vIndex - 1) == '_')) {
            vIndex = vIndex - 1;
        }
        return file.getName().substring(0, vIndex) + "." + ext;
    }

    public static String guessVersionNumber(String source) {
        String versionNumber = "";
        String fileName = source.substring(0, source.lastIndexOf("."));
        if (fileName.contains(".")) {
            String majorVersion = fileName.substring(0, fileName.indexOf("."));
            String minorVersion = fileName.substring(fileName.indexOf("."));
            int delimiter = majorVersion.lastIndexOf("-");
            if (majorVersion.indexOf("_") > delimiter) delimiter = majorVersion.indexOf("_");
            majorVersion = majorVersion.substring(delimiter + 1, fileName.indexOf("."));
            versionNumber = majorVersion + minorVersion;
        }
        return versionNumber;
    }
    
    public static String getBuildUrl(AbstractBuild<?, ?> build) {
        return build.getProject().getAbsoluteUrl() + String.valueOf(build.getNumber()) + "/";
    }

    public static String getBuildUrl(Run<?, ?> run) {
        return run.getParent().getAbsoluteUrl() + String.valueOf(run.getNumber()) + "/";
    }

    public static String getBuildNumber(AbstractBuild<?, ?> build) {
        return String.valueOf(build.getNumber());
    }

    public static String getBuildNumber(Run<?, ?> run) {
        return String.valueOf(run.getNumber());
    }

    public static String getJobUrl(AbstractBuild<?, ?> build) {
        return build.getProject().getAbsoluteUrl();
    }

    public static String getJobUrl(Run<?, ?> run) {
        return run.getParent().getAbsoluteUrl();
    }

    
    public static String getJobName(AbstractBuild<?, ?> build) {
        return build.getProject().getName();
    }

    public static String getJobName(Run<?, ?> run) {
        return run.getParent().getDisplayName();
    }



    public static String getInstanceUrl(AbstractBuild<?, ?> build, TaskListener listener) {
        String envValue = getEnvironmentVariable(build, listener, "JENKINS_URL");
        
        if (envValue != null) {
            return envValue;
        } else {
            String jobPath = "/job" + "/" + build.getProject().getName() + "/";
            int ind = build.getProject().getAbsoluteUrl().indexOf(jobPath);
            return build.getProject().getAbsoluteUrl().substring(0, ind);
        }
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

    public static String getScmUrl(AbstractBuild<?, ?> build, TaskListener listener) {
        if (isGitScm(build)) {
            return getEnvironmentVariable(build, listener, "GIT_URL");
        } else if (isSvnScm(build)) {
            return getEnvironmentVariable(build, listener, "SVN_URL");
        }
        
        return null;
    }

    public static String getScmBranch(AbstractBuild<?, ?> build, TaskListener listener) {
        if (isGitScm(build)) {
            return getEnvironmentVariable(build, listener, "GIT_BRANCH");
        } else if (isSvnScm(build)) {
            return null;
        }
        
        return null;
    }


    public static String getScmRevisionNumber(AbstractBuild<?, ?> build, TaskListener listener) {
        if (isGitScm(build)) {
            return getEnvironmentVariable(build, listener, "GIT_COMMIT");
        } else if (isSvnScm(build)) {
            return getEnvironmentVariable(build, listener, "SVN_REVISION");
        }
        
        return null;
    }
    
    public static boolean isGitScm(AbstractBuild<?, ?> build) {
        return "hudson.plugins.git.GitSCM".equalsIgnoreCase(build.getProject().getScm().getType());
    }


    public static boolean isSvnScm(AbstractBuild<?, ?> build) {
        return "hudson.scm.SubversionSCM".equalsIgnoreCase(build.getProject().getScm().getType());
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