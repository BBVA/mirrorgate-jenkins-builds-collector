package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.listener;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildStatus;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.DefaultMirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.builder.BuildBuilder;
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
        this.service = new DefaultMirrorGateService();
        
        LOG.fine(">>> MirrorGateRunListener Initialised");
    }

    @Override
    public void onDeleted(final Run run) {
        /* Do nothing */
    }

    @Override
    public void onStarted(final Run run, final TaskListener listener) {
        
        LOG.fine("onStarted starts");

        LOG.fine(run.toString());
        
        BuildBuilder builder = new BuildBuilder(run, BuildStatus.InProgress);
        
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
    
    protected MirrorGateService getMirrorGateService() {
        return service;
    }

}

