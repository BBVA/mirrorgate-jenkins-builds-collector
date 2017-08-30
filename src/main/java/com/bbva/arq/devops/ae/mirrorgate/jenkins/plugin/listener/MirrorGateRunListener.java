/*
 * Copyright 2017 Banco Bilbao Vizcaya Argentaria, S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.listener;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.builder.BuildBuilder;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildDTO;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildStatus;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.DefaultMirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateUtils;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.apache.commons.httpclient.HttpStatus;

@Extension
public class MirrorGateRunListener extends RunListener<Run> {

    protected static final Logger LOG = Logger.getLogger(MirrorGateRunListener.class.getName());

    private final MirrorGateService service;

    public MirrorGateRunListener() {
        this.service = new DefaultMirrorGateService();

        LOG.fine(">>> MirrorGateRunListener Initialised");
    }

    @Override
    public void onStarted(final Run run, final TaskListener listener) {

        LOG.fine("onStarted starts");

        LOG.fine(run.toString());

        BuildBuilder builder = new BuildBuilder(run, BuildStatus.InProgress);

        MirrorGateResponse buildResponse = publishBuild(builder.getBuildData());

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

        BuildBuilder builder = new BuildBuilder(run, BuildStatus.fromString(run.getResult().toString()));

        MirrorGateResponse buildResponse = publishBuild(builder.getBuildData());

        if (buildResponse.getResponseCode() == HttpStatus.SC_CREATED) {
            listener.getLogger().println("MirrorGate: Published Build Complete Data. " + buildResponse.toString());
        } else {
            listener.getLogger().println("MirrorGate: Failed Publishing Build Complete Data. " + buildResponse.toString());
        }

        LOG.fine("onCompleted ends");

    }

    protected MirrorGateService getMirrorGateService() {
        return service;
    }

    private MirrorGateResponse publishBuild(BuildDTO build) {
        String mirrorGateAPIUrl = MirrorGateUtils.getMirrorGateAPIUrl();
        String mirrorGateUser
                = MirrorGateUtils.getUsernamePasswordCredentials() != null
                        ? MirrorGateUtils.getUsernamePasswordCredentials()
                        .getUsername()
                        : null;
        String mirrorGatePassword
                = MirrorGateUtils.getUsernamePasswordCredentials() != null
                        ? MirrorGateUtils.getUsernamePasswordCredentials()
                        .getPassword().getPlainText() : null;

        return getMirrorGateService().publishBuildData(
                mirrorGateAPIUrl,
                build,
                mirrorGateUser,
                mirrorGatePassword);
    }
}
