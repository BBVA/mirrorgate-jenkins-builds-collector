package jenkins.plugins.mirrorgate;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpStatus;

import com.capitalone.dashboard.request.BuildDataCreateRequest;

import mirrorgate.utils.MirrorGateUtils;

//import org.json.simple.JSONArray;

public class DefaultMirrorGateService implements MirrorGateService {

    private static final Logger logger = Logger.getLogger(DefaultMirrorGateService.class.getName());

    private String mirrorGateAPIUrl = "";
    private boolean useProxy = false;


    public DefaultMirrorGateService(String mirrorGateAPIUrl, boolean useProxy) {
        super();
        this.mirrorGateAPIUrl = mirrorGateAPIUrl;
        this.useProxy = useProxy;
    }

    public void setMirrorGateAPIUrl(String mirrorGateAPIUrl) {
        this.mirrorGateAPIUrl = mirrorGateAPIUrl;
    }

    public MirrorGateResponse publishBuildData(BuildDataCreateRequest request) {
        String responseValue;
        int responseCode = HttpStatus.SC_NO_CONTENT;
        try {
            String jsonString = new String(MirrorGateUtils.convertObjectToJsonBytes(request));
            RestCall restCall = new RestCall(useProxy);
            RestCall.RestCallResponse callResponse = restCall.makeRestCallPost(mirrorGateAPIUrl + "/build", jsonString);
            responseCode = callResponse.getResponseCode();
            responseValue = callResponse.getResponseString().replaceAll("\"", "");
            if (responseCode != HttpStatus.SC_CREATED) {
                logger.log(Level.SEVERE, "mirrorGate: Build Publisher post may have failed. Response: " + responseCode);
            }
            return new MirrorGateResponse(responseCode, responseValue);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "mirrorGate: Error posting to mirrorGate", e);
            responseValue = "";
        }

        return new MirrorGateResponse(responseCode, responseValue);
    }

    public boolean testConnection() {
        RestCall restCall = new RestCall(useProxy);
        RestCall.RestCallResponse callResponse = restCall.makeRestCallGet(mirrorGateAPIUrl + "/health");
        int responseCode = callResponse.getResponseCode();

        if (responseCode == HttpStatus.SC_OK) return true;

        logger.log(Level.WARNING, "mirrorGate Test Connection Failed. Response: " + responseCode);
        return false;
    }
}
