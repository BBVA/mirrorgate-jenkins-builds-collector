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

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.DefaultMirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class MirrorGatePublisher extends Notifier {

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String mirrorGateAPIUrl;

        public DescriptorImpl() {
            load();
        }

        public String getMirrorGateAPIUrl() {
            return mirrorGateAPIUrl;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
            mirrorGateAPIUrl = sr.getParameter("mirrorGateAPIUrl");
            save();
            return super.configure(sr, formData);
        }

        public MirrorGateService getMirrorGateService() {
            return new DefaultMirrorGateService();
        }

        @Override
        public String getDisplayName() {
            return "MirrorGate Publisher";
        }

        public FormValidation doTestConnection(
            @QueryParameter("mirrorGateAPIUrl") final String mirrorGateAPIUrl) throws FormException {
            return getMirrorGateService().testConnection(mirrorGateAPIUrl) ? FormValidation.ok("Success") : FormValidation.error("Failure");
        }
    }
}
