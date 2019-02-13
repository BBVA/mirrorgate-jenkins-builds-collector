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
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

public class MirrorGateRecorder extends Recorder {

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor {

        private String mirrorGateAPIUrl;
        private String mirrorgateCredentialsId;
        private String extraURLs;

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class jobType) {
            return true;
        }

        public String getMirrorGateAPIUrl() {
            return mirrorGateAPIUrl;
        }

        public String getMirrorgateCredentialsId() {
            return mirrorgateCredentialsId;
        }

        public String getExtraURLs() {
            return extraURLs;
        }

        @Override
        public boolean configure(StaplerRequest sr, JSONObject formData)
                throws Descriptor.FormException {
            mirrorGateAPIUrl = sr.getParameter("mirrorGateAPIUrl");
            mirrorgateCredentialsId = sr.getParameter("_.mirrorgateCredentialsId");
            extraURLs = sr.getParameter("extraURLs");

            save();
            return super.configure(sr, formData);
        }

        public MirrorGateService getMirrorGateService() {
            return new DefaultMirrorGateService();
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return "MirrorGate Publisher";
        }

        public FormValidation doTestConnection(
                @QueryParameter("mirrorGateAPIUrl") final String mirrorGateAPIUrl,
                @QueryParameter("mirrorgateCredentialsId") final String credentialsId) {
            MirrorGateService testMirrorGateService = getMirrorGateService();
            MirrorGateResponse response
                    = testMirrorGateService.testConnection();
            return response.getResponseCode() == HttpStatus.SC_OK
                    ? FormValidation.ok("Success")
                    : FormValidation.error("Failure<"
                    + response.getResponseCode() + ">");
        }

        public ListBoxModel doFillMirrorgateCredentialsIdItems(
                @AncestorInPath Item item,
                @QueryParameter("mirrorgateCredentialsId") String credentialsId) {

            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            } else if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                return result.includeCurrentValue(credentialsId);
            }
            return result
                    .includeEmptyValue()
                    .includeAs(ACL.SYSTEM, item, StandardUsernamePasswordCredentials.class);
        }

        public FormValidation doCheckMirrorgateCredentialsId(
                @AncestorInPath Item item,
                @QueryParameter("mirrorgateCredentialsId") String credentialsId) {

            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            } else if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                return FormValidation.ok();
            }
            if (StringUtils.isBlank(credentialsId)) {
                return FormValidation.ok();
            }
            if (credentialsId.startsWith("${") && credentialsId.endsWith("}")) {
                return FormValidation.warning(
                        "Cannot validate expression based credentials");
            }
            return FormValidation.ok();
        }
    }

}
