package jenkins.plugins.mirrorgate;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import org.junit.Test;
import hudson.util.FormValidation;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.mockito.Spy;

public class MirrorGatePublisherTest {

    @Mock
    MirrorGateService service = mock(MirrorGateService.class);
        
    @Spy
    MirrorGatePublisher publisher = spy(new MirrorGatePublisher());
    
    @Spy
    MirrorGatePublisherStub.DescriptorImplStub descriptor  
            = spy(new MirrorGatePublisherStub.DescriptorImplStub());
    
    @Test
    public void testDoTestOKConnectionTest() throws Exception {
//        when(publisher.getDescriptor()).thenReturn(descriptor)

        when(service.testConnection(anyString())).thenReturn(true);
        when(descriptor.getMirrorGateService()).thenReturn(service);
        
        FormValidation result = descriptor.doTestConnection("http://localhost/");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }
    
    @Test
    public void testDoTestErrorConnectionTest() throws Exception {
//        when(publisher.getDescriptor()).thenReturn(descriptor)

        when(service.testConnection(anyString())).thenReturn(false);
        when(descriptor.getMirrorGateService()).thenReturn(service);
        
        FormValidation result = descriptor.doTestConnection("http://localhost/");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
    }
    
    @Test
    public void testDoTestConnectionWithoutServiveConnectionTest() throws Exception {
//        when(publisher.getDescriptor()).thenReturn(descriptor)

        when(service.testConnection(anyString())).thenReturn(false);
        when(descriptor.getMirrorGateService()).thenReturn(null);
        
        FormValidation result = descriptor.doTestConnection("http://localhost/");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
    }
    
}
