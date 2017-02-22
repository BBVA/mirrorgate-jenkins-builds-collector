package jenkins.plugins.hygieia;

import com.capitalone.dashboard.model.BuildStatus;
import com.capitalone.dashboard.model.RepoBranch;
import com.capitalone.dashboard.model.SCM;
import com.capitalone.dashboard.request.BinaryArtifactCreateRequest;
import com.capitalone.dashboard.request.BuildDataCreateRequest;
import com.capitalone.dashboard.request.CodeQualityCreateRequest;
import com.capitalone.dashboard.request.DeployDataCreateRequest;
import com.capitalone.dashboard.request.TestDataCreateRequest;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.scm.SubversionSCM;
import hygieia.builder.*;
import hygieia.utils.HygieiaUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.jenkinsci.plugins.multiplescms.MultiSCM;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("rawtypes")
public class ActiveNotifier implements FineGrainedNotifier {

    private static final Logger logger = Logger.getLogger(HygieiaListener.class.getName());

    private HygieiaPublisher publisher;
    private BuildListener listener;

    public ActiveNotifier(HygieiaPublisher publisher, BuildListener listener) {
        super();
        this.publisher = publisher;
        this.listener = listener;
    }

    private HygieiaService getHygieiaService(AbstractBuild r) {
        return publisher.newHygieiaService(r, listener);
    }

    public void started(AbstractBuild r) {
        boolean publish = (publisher.getHygieiaArtifact() != null) ||
                ((publisher.getHygieiaBuild() != null) && publisher.getHygieiaBuild().isPublishBuildStart()) ||
                ((publisher.getHygieiaTest() != null) && publisher.getHygieiaTest().isPublishTestStart()) ||
                ((publisher.getHygieiaSonar() != null) && publisher.getHygieiaSonar().isPublishBuildStart()) ||
                ((publisher.getHygieiaDeploy() != null) && publisher.getHygieiaDeploy().isPublishDeployStart());


        if (publish) {
            BuildBuilder builder = new BuildBuilder(r, publisher.getDescriptor().getHygieiaJenkinsName(), listener, false, true);
            HygieiaResponse response = getHygieiaService(r).publishBuildData(builder.getBuildData());
            if (response.getResponseCode() == HttpStatus.SC_CREATED) {
                listener.getLogger().println("Hygieia: Published Build Complete Data. " + response.toString());
            } else {
                listener.getLogger().println("Hygieia: Failed Publishing Build Complete Data. " + response.toString());
            }
        }

    }

    public void deleted(AbstractBuild r) {
    }


    public void finalized(AbstractBuild r) {

    }

    public void completed(AbstractBuild r) {
        boolean publishBuild = (publisher.getHygieiaArtifact() != null) || (publisher.getHygieiaSonar() != null) ||
                (publisher.getHygieiaBuild() != null) || (publisher.getHygieiaTest() != null) || (publisher.getHygieiaDeploy() != null);

        if (publishBuild) {
            BuildBuilder builder = new BuildBuilder(r, publisher.getDescriptor().getHygieiaJenkinsName(), listener, true, true);
            HygieiaResponse buildResponse = getHygieiaService(r).publishBuildData(builder.getBuildData());
            if (buildResponse.getResponseCode() == HttpStatus.SC_CREATED) {
                listener.getLogger().println("Hygieia: Published Build Complete Data. " + buildResponse.toString());
            } else {
                listener.getLogger().println("Hygieia: Failed Publishing Build Complete Data. " + buildResponse.toString());
            }

            boolean successBuild = ("success".equalsIgnoreCase(r.getResult().toString()) ||
                    "unstable".equalsIgnoreCase(r.getResult().toString()));
            boolean publishArt = (publisher.getHygieiaArtifact() != null) && successBuild;

            if (publishArt) {
                ArtifactBuilder artifactBuilder = new ArtifactBuilder(r, publisher, listener, buildResponse.getResponseValue());
                Set<BinaryArtifactCreateRequest> requests = artifactBuilder.getArtifacts();
                for (BinaryArtifactCreateRequest bac : requests) {
                    HygieiaResponse artifactResponse = getHygieiaService(r).publishArtifactData(bac);
                    if (artifactResponse.getResponseCode() == HttpStatus.SC_CREATED) {
                        listener.getLogger().println("Hygieia: Published Build Artifact Data. Filename=" +
                                bac.getCanonicalName() + ", Name=" + bac.getArtifactName() + ", Version=" + bac.getArtifactVersion() +
                                ", Group=" + bac.getArtifactGroup() + ". " + artifactResponse.toString());
                    } else {
                        listener.getLogger().println("Hygieia: Failed Publishing Build Artifact Data. " + bac.getCanonicalName() + ", Name=" + bac.getArtifactName() + ", Version=" + bac.getArtifactVersion() +
                                ", Group=" + bac.getArtifactGroup() + ". " + artifactResponse.toString());
                    }
                }
            }

            boolean publishTest = (publisher.getHygieiaTest() != null) && (successBuild || publisher.getHygieiaTest().isPublishEvenBuildFails());

            if (publishTest) {
//                CucumberTestBuilder(Run run, TaskListener listener, BuildStatus buildStatus, FilePath filePath, String applicationName, String environmentName, String testType, String filePattern, String directory, String jenkinsName, String buildId)
                BuildStatus buildStatus = BuildStatus.fromString(r.getResult().toString());
                CucumberTestBuilder cucumberTestBuilder = new CucumberTestBuilder(r, listener, buildStatus, r.getWorkspace(), publisher.getHygieiaTest().getTestApplicationName(),
                        publisher.getHygieiaTest().getTestEnvironmentName(), publisher.getHygieiaTest().getTestType(), publisher.getHygieiaTest().getTestFileNamePattern(), publisher.getHygieiaTest().getTestResultsDirectory(),
                        publisher.getDescriptor().getHygieiaJenkinsName(), buildResponse.getResponseValue());
                TestDataCreateRequest request = cucumberTestBuilder.getTestDataCreateRequest();
                if (request != null) {
                    HygieiaResponse testResponse = getHygieiaService(r).publishTestResults(request);
                    if (testResponse.getResponseCode() == HttpStatus.SC_CREATED) {
                        listener.getLogger().println("Hygieia: Published Test Data. " + testResponse.toString());
                    } else {
                        listener.getLogger().println("Hygieia: Failed Publishing Test Data. " + testResponse.toString());
                    }
                } else {
                    listener.getLogger().println("Hygieia: Published Test Data. Nothing to publish");
                }
            }

            boolean publishSonar = (publisher.getHygieiaSonar() != null) && successBuild;

            if (publishSonar) {
                try {
                    SonarBuilder sonarBuilder = new SonarBuilder(r, listener, publisher.getDescriptor().getHygieiaJenkinsName(), publisher.getHygieiaSonar().getCeQueryIntervalInSeconds(),
                            publisher.getHygieiaSonar().getCeQueryMaxAttempts(), buildResponse.getResponseValue(), publisher.getDescriptor().isUseProxy());
                    CodeQualityCreateRequest request = sonarBuilder.getSonarMetrics();
                    if (request != null) {
                        HygieiaResponse sonarResponse = getHygieiaService(r).publishSonarResults(request);
                        if (sonarResponse.getResponseCode() == HttpStatus.SC_CREATED) {
                            listener.getLogger().println("Hygieia: Published Sonar Data. " + sonarResponse.toString());
                        } else {
                            listener.getLogger().println("Hygieia: Failed Publishing Sonar Data. " + sonarResponse.toString());
                        }
                    } else {
                        listener.getLogger().println("Hygieia: Published Sonar Result. Nothing to publish");
                    }
                } catch (IOException | URISyntaxException | ParseException e) {
                    listener.getLogger().println("Hygieia: Publishing error" + '\n' + e.getMessage());
                }

            }

            boolean publishDeploy = (publisher.getHygieiaDeploy() != null) && successBuild;
            if (publishDeploy) {
                DeployBuilder deployBuilder = new DeployBuilder(r, publisher, listener, buildResponse.getResponseValue());
                Set<DeployDataCreateRequest> requests = deployBuilder.getDeploys();
                for (DeployDataCreateRequest bac : requests) {
                    HygieiaResponse deployResponse = getHygieiaService(r).publishDeployData(bac);
                    if (deployResponse.getResponseCode() == HttpStatus.SC_CREATED) {
                        listener.getLogger().println("Hygieia: Published Deploy Data: " + deployResponse.toString());
                    } else {
                        listener.getLogger().println("Hygieia: Failed Publishing Deploy Data:" + deployResponse.toString());
                    }
                }
            }
        }
    }
}
