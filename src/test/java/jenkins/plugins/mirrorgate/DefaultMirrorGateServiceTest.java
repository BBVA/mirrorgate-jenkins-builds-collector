package jenkins.plugins.mirrorgate;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildDataCreateRequest;
import org.junit.Test;

public class DefaultMirrorGateServiceTest {

    /**
     * Publish should generally not rethrow exceptions, or it will cause a build job to fail at end.
     */
    @Test
    public void publishWithBadHostShouldNotRethrowExceptions() {
        DefaultMirrorGateService service = new DefaultMirrorGateService("foo");
        service.setMirrorGateAPIUrl("hostvaluethatwillcausepublishtofail");

        service.publishBuildData(makeBuildDataRequestData());
    }


    /**
     * Use a valid team domain, but a bad token
     */
    @Test
    public void invalidTokenShouldFail() {
        DefaultMirrorGateService service = new DefaultMirrorGateService("tinyspeck");
        service.publishBuildData(makeBuildDataRequestData());
    }


//    @Test
//    public void successfulPublishBuildDataReturnsTrue() {
//        DefaultMirrorGateServiceStub service = new DefaultMirrorGateServiceStub("domain", "token");
//        HttpClientStub httpClientStub = new HttpClientStub();
//        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
//        service.setHttpClient(httpClientStub);
//        assertTrue(service.publishBuildData(makeBuildDataRequestData()));
//    }

    private BuildDataCreateRequest makeBuildDataRequestData() {
        BuildDataCreateRequest build = new BuildDataCreateRequest();
        build.setNumber("1");
        build.setBuildUrl("buildUrl");
        build.setStartTime(3);
        build.setEndTime(8);
        build.setDuration(5);
        build.setBuildStatus("Success");
        build.setStartedBy("foo");
        return build;
    }

}
