package jenkins.plugins.mirrorgate;

import org.apache.commons.httpclient.HttpStatus;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import mirrorgate.builder.BuildBuilder;

@SuppressWarnings("rawtypes")
public class ActiveNotifier implements FineGrainedNotifier {

    private final MirrorGatePublisher publisher;
    private final BuildListener listener;

    public ActiveNotifier(MirrorGatePublisher publisher, BuildListener listener) {
        super();
        this.publisher = publisher;
        this.listener = listener;
    }

    private MirrorGateService getMirrorGateService(AbstractBuild r) {
        return publisher.newMirrorGateService(r, listener);
    }

    @Override
    public void started(AbstractBuild r) {
        boolean publish = ((publisher.getMirrorGateBuild() != null) && publisher.getMirrorGateBuild().isPublishBuildStart());


        if (publish) {
            BuildBuilder builder = new BuildBuilder(r, false, true);
            MirrorGateResponse response = getMirrorGateService(r).publishBuildData(builder.getBuildData());
            if (response.getResponseCode() == HttpStatus.SC_CREATED) {
                listener.getLogger().println("MirrorGate: Published Build Complete Data. " + response.toString());
            } else {
                listener.getLogger().println("MirrorGate: Failed Publishing Build Complete Data. " + response.toString());
            }
        }

    }

    @Override
    public void deleted(AbstractBuild r) {
    }


    @Override
    public void finalized(AbstractBuild r) {

    }

    public void completed(AbstractBuild r) {
        boolean publishBuild = (publisher.getMirrorGateBuild() != null);

        if (publishBuild) {
            BuildBuilder builder = new BuildBuilder(r, true, true);
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
