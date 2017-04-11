package jenkins.plugins.mirrorgate;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildStatus;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.util.logging.Logger;
import mirrorgate.builder.BuildBuilder;
import org.apache.commons.httpclient.HttpStatus;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class MirrorGateRunListener extends RunListener<Run> {

    protected static final Logger LOG = Logger.getLogger(MirrorGateRunListener.class.getName());
        
    private final MirrorGateService service;

    /**
     * This class is lazy loaded (as required).
     */
    public MirrorGateRunListener() {
        MirrorGatePublisher.DescriptorImpl mirrorGateDesc = Jenkins.getInstance().getDescriptorByType(MirrorGatePublisher.DescriptorImpl.class);
        this.service = new DefaultMirrorGateService(mirrorGateDesc.getMirrorGateAPIUrl());
        
        LOG.fine(">>> Initialised");
    }

    @Override
    public void onDeleted(final Run run) {
        
        LOG.fine("onDeleted starts");

        LOG.fine(run.toString());
        
        BuildBuilder builder = new BuildBuilder(run, BuildStatus.Deleted, true);
        MirrorGateResponse buildResponse = service.publishBuildData(builder.getBuildData());
        
        LOG.fine("onDeleded ends");
    }

    @Override
    public void onStarted(final Run run, final TaskListener listener) {
        
        LOG.fine("onStarted starts");

        LOG.fine(run.toString());
        
        BuildBuilder builder = new BuildBuilder(run, BuildStatus.InProgress, true);
                
        MirrorGateResponse buildResponse = service.publishBuildData(builder.getBuildData());
        
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

        BuildBuilder  builder = new BuildBuilder(run, BuildStatus.fromString(run.getResult().toString()), true);
        
        LOG.fine(builder.getBuildData().getBuildUrl());
                
        MirrorGateResponse buildResponse = service.publishBuildData(builder.getBuildData());
        
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

}

