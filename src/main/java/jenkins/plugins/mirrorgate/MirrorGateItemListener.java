package jenkins.plugins.mirrorgate;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildStatus;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import java.util.logging.Level;
import jenkins.model.Jenkins;

import java.util.logging.Logger;
import mirrorgate.builder.BuildBuilder;
import org.apache.commons.httpclient.HttpStatus;

/**
 * Warning: This MUST stay a Java class, Groovy cannot compile (for some reason??).
 */
@Extension
public class MirrorGateItemListener extends ItemListener {

    protected static final Logger LOG = Logger.getLogger(MirrorGateItemListener.class.getName());

    /**
     * This class is lazy loaded (as required).
     */
    public MirrorGateItemListener() {
        LOG.fine(">>> MirrorGateItemListener Initialised");
    }

    @Override
    public void onDeleted(final Item item) {
        
        LOG.fine("onDeletedItem starts");

        item.getAllJobs().forEach((job) -> {
            
            if(job.getLastBuild() != null) {
            
                BuildBuilder  builder = new BuildBuilder(job.getLastBuild(), BuildStatus.Deleted);
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
    
    private MirrorGateService getMirrorGateService(){
        MirrorGatePublisher.DescriptorImpl mirrorGateDesc = Jenkins.getInstance().getDescriptorByType(MirrorGatePublisher.DescriptorImpl.class);
        return new DefaultMirrorGateService(mirrorGateDesc.getMirrorGateAPIUrl());
    }

}

