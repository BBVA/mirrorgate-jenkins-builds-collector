package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.listener;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.Item;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import hudson.model.Job;
import hudson.model.Run;
import java.util.Arrays;
import java.util.Collection;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.apache.commons.httpclient.HttpStatus;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Job.class})
public class MirrorGateItemListenerTest extends TestCase {

    @Mock
    Item item;

    @Mock
    MirrorGateService service;
    
    @Spy
    MirrorGateItemListener listener = new MirrorGateItemListener();
    
    Job[] jobs = new Job[10];

    private final MirrorGateResponse responseOk = new MirrorGateResponse(HttpStatus.SC_CREATED, "");
    private final MirrorGateResponse responseError = new MirrorGateResponse(HttpStatus.SC_FORBIDDEN, "");

    private final String buildSample = "http://localhost:8080/job/MirrorGate/job/mirrorgate-jenkins-plugin/job/test/5/";
    
    @Before
    @Override
    public void setUp() {
        item = mock(Item.class);
        service = mock(MirrorGateService.class);

        Arrays.fill(jobs, createMockingJob());
        
        when(item.getAllJobs()).thenReturn((Collection) Arrays.asList(jobs));
        when(listener.getMirrorGateService()).thenReturn(service);
    }
        
    @Test
    public void onDeletedTestWhenServiceResponseOK() {
        when(service.publishBuildData(any())).thenReturn(responseOk);
        
        listener.onDeleted(item);
        
        verify(service, times(jobs.length)).publishBuildData(any());
    }
    
    @Test
    public void onDeletedTestWhenServiceResponseError() {
        when(service.publishBuildData(any())).thenReturn(responseError);
        
        listener.onDeleted(item);
        
        /* Should follow trying*/
        verify(service, times(jobs.length)).publishBuildData(any());
    }
    
    private Job createMockingJob() {
        Job job = PowerMockito.mock(Job.class);
        Run build = mock(Run.class);
        
        when(job.getLastBuild()).thenReturn(build);
        PowerMockito.when(job.getAbsoluteUrl()).thenReturn(buildSample);
        when(build.getParent()).thenReturn(job);
        
        return job;
    }
}

