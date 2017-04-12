package jenkins.plugins.mirrorgate;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildStatus;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import mirrorgate.builder.BuildBuilder;
import org.apache.commons.httpclient.HttpStatus;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class MirrorGateRunListener extends RunListener<Run> {

    protected static final Logger LOG = Logger.getLogger(MirrorGateRunListener.class.getName());

    /**
     * This class is lazy loaded (as required).
     */
    public MirrorGateRunListener() {
        LOG.fine(">>> MirrorGateRunListener Initialised");
    }

    @Override
    public void onDeleted(final Run run) {
        
        LOG.fine("onDeleted starts");

        LOG.fine(run.toString());
        
        /* A deleted build is not marked as a deleted on publish service*/
        
//        BuildBuilder builder = new BuildBuilder(run, BuildStatus.Deleted, true);
//        MirrorGateResponse buildResponse = getMirrorGateService().publishBuildData(builder.getBuildData());
//        
//        if (buildResponse.getResponseCode() == HttpStatus.SC_CREATED) {
//            LOG.log(Level.WARNING, "MirrorGate: Published Build Complete Data. {0}", buildResponse.toString());
//        } else {
//            LOG.log(Level.FINE, "MirrorGate: Failed Publishing Build Complete Data. {0}", buildResponse.toString());
//        }
        
        LOG.fine("onDeleded ends");
    }

    @Override
    public void onStarted(final Run run, final TaskListener listener) {
        
        LOG.fine("onStarted starts");

        LOG.fine(run.toString());
        
        BuildBuilder builder = new BuildBuilder(run, BuildStatus.InProgress);
        
        MirrorGatePublisher.DescriptorImpl mirrorGateDesc = Jenkins.getInstance().getDescriptorByType(MirrorGatePublisher.DescriptorImpl.class);
        MirrorGateResponse buildResponse = getMirrorGateService().publishBuildData(builder.getBuildData());
        
        if (buildResponse.getResponseCode() == HttpStatus.SC_CREATED) {
            listener.getLogger().println("MirrorGate: Published Build Complete Data. " + buildResponse.toString());
        } else {
            listener.getLogger().println("MirrorGate: Failed Publishing Build Complete Data. " + buildResponse.toString());
        }
    
        LOG.fine("onStarted ends");

    }

    @Override
    public void onCompleted(final Run run, final @Nonnull TaskListener listener) {
        
        LOG.fine("onCompleted starts");

        LOG.fine(run.toString());

        BuildBuilder  builder = new BuildBuilder(run, BuildStatus.fromString(run.getResult().toString()));
                        
        MirrorGateResponse buildResponse = getMirrorGateService().publishBuildData(builder.getBuildData());
        
        if (buildResponse.getResponseCode() == HttpStatus.SC_CREATED) {
            listener.getLogger().println("MirrorGate: Published Build Complete Data. " + buildResponse.toString());
        } else {
            listener.getLogger().println("MirrorGate: Failed Publishing Build Complete Data. " + buildResponse.toString());
        }
      
        LOG.fine("onCompleted ends");

    }
    
        @Override
    public void onFinalized(final Run run) {

    }
    
    private MirrorGateService getMirrorGateService(){
        MirrorGatePublisher.DescriptorImpl mirrorGateDesc = Jenkins.getInstance().getDescriptorByType(MirrorGatePublisher.DescriptorImpl.class);
        return new DefaultMirrorGateService(mirrorGateDesc.getMirrorGateAPIUrl());
    }

}

