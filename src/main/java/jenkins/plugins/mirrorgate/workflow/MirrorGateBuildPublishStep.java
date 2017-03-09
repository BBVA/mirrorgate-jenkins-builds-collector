package jenkins.plugins.mirrorgate.workflow;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpStatus;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.capitalone.dashboard.model.BuildStatus;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.plugins.mirrorgate.DefaultMirrorGateService;
import jenkins.plugins.mirrorgate.MirrorGatePublisher;
import jenkins.plugins.mirrorgate.MirrorGateResponse;
import jenkins.plugins.mirrorgate.MirrorGateService;
import mirrorgate.builder.BuildBuilder;


public class MirrorGateBuildPublishStep extends AbstractStepImpl {


    private String buildStatus;

    public String getBuildStatus() {
        return buildStatus;
    }

    @DataBoundSetter
    public void setBuildStatus(String buildStatus) {
        this.buildStatus = buildStatus;
    }

    @DataBoundConstructor
    public MirrorGateBuildPublishStep(@Nonnull String buildStatus) {
        this.buildStatus = buildStatus;
    }


    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(MirrorGateBuildPublishStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "mirrorGateBuildPublishStep";
        }

        @Override
        public String getDisplayName() {
            return "MirrorGate Build Publish Step";
        }


        public ListBoxModel doFillBuildStatusItems() {
            ListBoxModel model = new ListBoxModel();

            model.add("Started", "InProgress");
            model.add("Success", BuildStatus.Success.toString());
            model.add("Failure", BuildStatus.Failure.toString());
            model.add("Unstable", BuildStatus.Unstable.toString());
            model.add("Aborted", BuildStatus.Aborted.toString());
            return model;
        }

    }

    public static class MirrorGateBuildPublishStepExecution extends AbstractSynchronousNonBlockingStepExecution<Integer> {

        private static final long serialVersionUID = 1L;

        @Inject
        transient MirrorGateBuildPublishStep step;

        @StepContextParameter
        transient TaskListener listener;

        @StepContextParameter
        transient Run run;

        // This run MUST return a non-Void object, otherwise it will be executed three times!!!! No idea why
        @Override
        protected Integer run() throws Exception {

            //default to global config values if not set in step, but allow step to override all global settings

            Jenkins jenkins;
            try {
                jenkins = Jenkins.getInstance();
            } catch (NullPointerException ne) {
                listener.error(ne.toString());
                return -1;
            }

            MirrorGatePublisher.DescriptorImpl mirrorGateDesc = jenkins.getDescriptorByType(MirrorGatePublisher.DescriptorImpl.class);
            MirrorGateService mirrorGateService = getMirrorGateService(mirrorGateDesc.getMirrorGateAPIUrl(), mirrorGateDesc.isUseProxy());
            BuildBuilder builder = new BuildBuilder(run, mirrorGateDesc.getMirrorGateJenkinsName(), listener, BuildStatus.fromString(step.buildStatus), true);
            MirrorGateResponse buildResponse = mirrorGateService.publishBuildData(builder.getBuildData());
            if (buildResponse.getResponseCode() == HttpStatus.SC_CREATED) {
                listener.getLogger().println("MirrorGate: Published Build Complete Data. " + buildResponse.toString());
            } else {
                listener.getLogger().println("MirrorGate: Failed Publishing Build Complete Data. " + buildResponse.toString());
            }

            return buildResponse.getResponseCode();
        }


        //streamline unit testing
        MirrorGateService getMirrorGateService(String mirrorGateAPIUrl, boolean useProxy) {
            return new DefaultMirrorGateService(mirrorGateAPIUrl, useProxy);
        }
    }

}