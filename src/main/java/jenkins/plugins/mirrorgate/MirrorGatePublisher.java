package jenkins.plugins.mirrorgate;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

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

    public MirrorGateService newMirrorGateService(Run r, TaskListener listener) {
        EnvVars env;
        try {
            env = r.getEnvironment(listener);
        } catch (IOException | InterruptedException e) {
            listener.getLogger().println("Error retrieving environment vars: " + e.getMessage());
            env = new EnvVars();
        }
        return makeService(env);
    }

    private MirrorGateService makeService(EnvVars env) {
        String mirrorGateAPIUrl = getDescriptor().getMirrorGateAPIUrl();
        mirrorGateAPIUrl = env.expand(mirrorGateAPIUrl);
        return new DefaultMirrorGateService(mirrorGateAPIUrl);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
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

        public MirrorGateService getMirrorGateService(final String mirrorGateAPIUrl) {
            return new DefaultMirrorGateService(mirrorGateAPIUrl);
        }

        @Override
        public String getDisplayName() {
            return "MirrorGate Publisher";
        }

        public FormValidation doTestConnection(
                @QueryParameter("mirrorGateAPIUrl") final String mirrorGateAPIUrl) throws FormException {

            String hostUrl = mirrorGateAPIUrl;
            if (StringUtils.isEmpty(hostUrl)) {
                hostUrl = this.mirrorGateAPIUrl;
            }

            MirrorGateService testMirrorGateService = getMirrorGateService(hostUrl);
            if (testMirrorGateService != null) {
                boolean success = testMirrorGateService.testConnection();
                return success ? FormValidation.ok("Success") : FormValidation.error("Failure");
            } else {
                return FormValidation.error("Failure");
            }
        }

        public FormValidation doCheckValue(@QueryParameter String value) throws IOException, ServletException {
            if (value.isEmpty()) {
                return FormValidation.warning("You must fill this box!");
            }
            return FormValidation.ok();
        }

    }
}
