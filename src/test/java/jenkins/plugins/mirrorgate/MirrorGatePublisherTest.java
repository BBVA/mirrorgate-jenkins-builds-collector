/*
 * Copyright 2017 Banco Bilbao Vizcaya Argentaria, S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jenkins.plugins.mirrorgate;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import hudson.util.FormValidation;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

public class MirrorGatePublisherTest {

    @Mock
    MirrorGateService service = mock(MirrorGateService.class);

    @Spy
    MirrorGatePublisherStub.DescriptorImplStub descriptor
            = spy(new MirrorGatePublisherStub.DescriptorImplStub());

    @Test
    public void testDoTestOKConnectionTest() throws Exception {
        when(service.testConnection(anyString())).thenReturn(true);
        when(descriptor.getMirrorGateService()).thenReturn(service);

        FormValidation result = descriptor.doTestConnection("http://localhost/");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    public void testDoTestErrorConnectionTest() throws Exception {
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
