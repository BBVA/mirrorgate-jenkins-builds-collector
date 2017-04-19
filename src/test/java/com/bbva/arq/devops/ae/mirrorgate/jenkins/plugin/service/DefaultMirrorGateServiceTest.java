package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildDataCreateRequest;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.RestCall;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.Spy;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DefaultMirrorGateServiceTest extends TestCase {
    
    @Mock
    HttpClient htppClient;
    
    @Spy
    RestCall restCall;
    
    @Spy
    DefaultMirrorGateService service;
    
    @Before
    @Override
    public void setUp() {
        htppClient = mock(HttpClient.class);
        restCall = spy(new RestCall());
        service = spy(new DefaultMirrorGateService());
        
        when(restCall.getHttpClient()).thenReturn(htppClient);
        when(service.buildRestCall()).thenReturn(restCall);
    }

    @Test
    public void testSuccessfulPublishBuildDataTest() throws IOException {
        when(htppClient.executeMethod(any(PostMethod.class))).thenReturn(HttpStatus.SC_OK);

        MirrorGateResponse response = service.publishBuildData(makeBuildDataRequestData());
        assertEquals(HttpStatus.SC_OK, response.getResponseCode());
    }
    
    @Test
    public void testFailedPublishBuildDataTest() throws IOException {
        when(htppClient.executeMethod(any(PostMethod.class))).thenReturn(HttpStatus.SC_FORBIDDEN);

        MirrorGateResponse response = service.publishBuildData(makeBuildDataRequestData());
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getResponseCode());
    }

    @Test
    public void testSuccessConnectionTest() throws IOException {
        when(htppClient.executeMethod(any(GetMethod.class))).thenReturn(HttpStatus.SC_OK);
        assertTrue(service.testConnection("http://localhost/"));
    }
    
    
    @Test
    public void testFailedConnectionTest() throws IOException {
        when(htppClient.executeMethod(any(GetMethod.class))).thenReturn(HttpStatus.SC_FORBIDDEN);
        
        assertFalse(service.testConnection("http://localhost/"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConnectionWithBadHostUrlFormatTest() throws IOException {
        when(htppClient.executeMethod(any(GetMethod.class))).thenReturn(HttpStatus.SC_OK);
        
        service.testConnection("");
    }

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
