package jenkins.plugins.mirrorgate;

import org.apache.commons.httpclient.HttpStatus;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import mirrorgate.builder.BuildBuilder;

@SuppressWarnings("rawtypes")
public class ActiveNotifier implements FineGrainedNotifier {

    private MirrorGatePublisher publisher;
    private BuildListener listener;

    public ActiveNotifier(MirrorGatePublisher publisher, BuildListener listener) {
        super();
        this.publisher = publisher;
        this.listener = listener;
    }

    private MirrorGateService getMirrorGateService(AbstractBuild r) {
        return publisher.newMirrorGateService(r, listener);
    }

    public void started(AbstractBuild r) {
        boolean publish = ((publisher.getMirrorGateBuild() != null) && publisher.getMirrorGateBuild().isPublishBuildStart());


        if (publish) {
            BuildBuilder builder = new BuildBuilder(r, publisher.getDescriptor().getMirrorGateJenkinsName(), listener, false, true);
            MirrorGateResponse response = getMirrorGateService(r).publishBuildData(builder.getBuildData());
            if (response.getResponseCode() == HttpStatus.SC_CREATED) {
                listener.getLogger().println("MirrorGate: Published Build Complete Data. " + response.toString());
            } else {
                listener.getLogger().println("MirrorGate: Failed Publishing Build Complete Data. " + response.toString());
            }
        }

    }

    public void deleted(AbstractBuild r) {
    }


    public void finalized(AbstractBuild r) {

    }

    public void completed(AbstractBuild r) {
        boolean publishBuild = (publisher.getMirrorGateBuild() != null);

        if (publishBuild) {
            BuildBuilder builder = new BuildBuilder(r, publisher.getDescriptor().getMirrorGateJenkinsName(), listener, true, true);
            MirrorGateResponse buildResponse = getMirrorGateService(r).publishBuildData(builder.getBuildData());
            if (buildResponse.getResponseCode() == HttpStatus.SC_CREATED) {
                listener.getLogger().println("MirrorGate: Published Build Complete Data. " + buildResponse.toString());
            } else {
                listener.getLogger().println("MirrorGate: Failed Publishing Build Complete Data. " + buildResponse.toString());
            }

            boolean successBuild = ("success".equalsIgnoreCase(r.getResult().toString()) ||
                    "unstable".equalsIgnoreCase(r.getResult().toString()));
        }
    }
}
