package jenkins.plugins.mirrorgate;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildDataCreateRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
import junit.framework.TestCase;

@RunWith(Parameterized.class)
public class MirrorGatePublisherTest extends TestCase {

    private MirrorGatePublisherStub.DescriptorImplStub descriptor;
    private MirrorGateServiceStub mirrorGateServiceStub;
    private boolean responseBoolean;
    private MirrorGateResponse mirrorGateResponse;
    private FormValidation.Kind expectedResult;

    @Before
    @Override
    public void setUp() {
        descriptor = new MirrorGatePublisherStub.DescriptorImplStub();
    }

    public MirrorGatePublisherTest(MirrorGateServiceStub mirrorGateServiceStub, boolean responseBoolean, FormValidation.Kind expectedResult) {
        this.mirrorGateServiceStub = mirrorGateServiceStub;
        this.responseBoolean = responseBoolean;
//        this.responseString = responseString;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection businessTypeKeys() {
        return Arrays.asList(new Object[][]{
                {new MirrorGateServiceStub(), true, FormValidation.Kind.OK},
                {new MirrorGateServiceStub(), false, FormValidation.Kind.ERROR},
                {null, false, FormValidation.Kind.ERROR}
        });
    }

    @Test
    public void testDoTestConnection() throws Exception {
        if (mirrorGateServiceStub != null) {
            mirrorGateServiceStub.setResponse(responseBoolean);
            mirrorGateServiceStub.setMirrorGateResponse(mirrorGateResponse);
        }
        descriptor.setMirrorGateService(mirrorGateServiceStub);
        try {
            FormValidation result = descriptor.doTestConnection("mirrorGateUrl");
            assertEquals(result.kind, expectedResult);
        } catch (Descriptor.FormException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public static class MirrorGateServiceStub implements MirrorGateService {

        private boolean responseBoolean;
        private MirrorGateResponse mirrorGateResponse;


        public void setResponse(boolean response) {
            this.responseBoolean = response;
        }

        public MirrorGateResponse getMirrorGateResponse() {
            return mirrorGateResponse;
        }

        public void setMirrorGateResponse(MirrorGateResponse mirrorGateResponse) {
            this.mirrorGateResponse = mirrorGateResponse;
        }

        public MirrorGateResponse publishBuildData(BuildDataCreateRequest request) {
            return mirrorGateResponse;
        }

        public boolean testConnection() {
            return responseBoolean;
        }

        public Set<String> getDeploymentEnvironments(String appName) {
            return null;
        }

    }
}
