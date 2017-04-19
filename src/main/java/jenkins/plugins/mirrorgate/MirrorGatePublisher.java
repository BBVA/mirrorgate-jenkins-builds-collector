package jenkins.plugins.mirrorgate;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.DefaultMirrorGateService;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;

//import org.json.simple.JSONObject;

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
            
            String hostUrl = mirrorGateAPIUrl;
            
            MirrorGateService testMirrorGateService = getMirrorGateService();
            if (testMirrorGateService != null) {
                boolean success = testMirrorGateService.testConnection(hostUrl);
                return success ? FormValidation.ok("Success") : FormValidation.error("Failure");
            } else {
                return FormValidation.error("Failure");
            }
        }
    }
}
