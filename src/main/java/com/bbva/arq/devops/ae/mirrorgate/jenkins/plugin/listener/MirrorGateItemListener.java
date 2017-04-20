package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.listener;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildStatus;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import java.util.logging.Level;

import java.util.logging.Logger;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.DefaultMirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.builder.BuildBuilder;
import org.apache.commons.httpclient.HttpStatus;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class MirrorGateItemListener extends ItemListener {

    private static final Logger LOG = Logger.getLogger(MirrorGateItemListener.class.getName());
    
    private final MirrorGateService service;

    /**
     * This class is lazy loaded (as required).
     */
    public MirrorGateItemListener() {
        this.service = new DefaultMirrorGateService();
        
        LOG.fine(">>> MirrorGateItemListener Initialised");
    }

    @Override
    public void onDeleted(final Item item) {
        
        LOG.fine("onDeletedItem starts");

        item.getAllJobs().forEach((job) -> {
            
            if(job.getLastBuild() != null) {
            
                BuildBuilder builder = new BuildBuilder(job.getLastBuild(), BuildStatus.Deleted);
                MirrorGateResponse buildResponse = getMirrorGateService().publishBuildData(builder.getBuildData());

                if (buildResponse.getResponseCode() == HttpStatus.SC_CREATED) {
                    LOG.log(Level.WARNING, "MirrorGate: Published Build Complete Data. {0}", buildResponse.toString());
                } else {
                    LOG.log(Level.FINE, "MirrorGate: Failed Publishing Build Complete Data. {0}", buildResponse.toString());
                }
            
            }
        
        });
 
        LOG.fine("onDeletedItem ends");
    }
    
    protected MirrorGateService getMirrorGateService() {
        return service;
    }
}

