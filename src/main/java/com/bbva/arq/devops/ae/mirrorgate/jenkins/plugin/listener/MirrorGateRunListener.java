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
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.console.ImgConsoleNote;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildStatus;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.DefaultMirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateUtils;
import hudson.Extension;
import hudson.console.HyperlinkNote;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.httpclient.HttpStatus;

@Extension
public class MirrorGateRunListener extends RunListener<Run> {

    private static final Logger LOG = Logger.getLogger(MirrorGateRunListener.class.getName());

    private final MirrorGateService service;


    public MirrorGateRunListener() {
        this.service = new DefaultMirrorGateService();

        LOG.fine(">>> MirrorGateRunListener Initialised");
    }

    @Override
    public void onStarted(final Run run, final TaskListener listener) {

        LOG.fine("onStarted starts");

        BuildBuilder builder = new BuildBuilder(run, BuildStatus.InProgress);

        MirrorGateResponse buildResponse = getMirrorGateService()
            .publishBuildData(builder.getBuildData());

        listener.getLogger().println("Follow this project's builds progress at: "
            + createMirrorgateLink(builder.getBuildData().getProjectName()));

        if (buildResponse.getResponseCode() == HttpStatus.SC_CREATED) {
            listener.getLogger().println("MirrorGate: Published Build "
                + "Complete Data. " + buildResponse.toString());
        } else {
            listener.getLogger().println("MirrorGate: Failed Publishing "
                + "Build Complete Data. " + buildResponse.toString());
        }

        sendBuildExtraData(builder, listener);

        LOG.fine("onStarted ends");
    }

    @Override
    public void onCompleted(final Run run, final @Nonnull TaskListener listener) {

        LOG.fine("onCompleted starts");

        LOG.fine(run.toString());

        BuildBuilder builder = new BuildBuilder(
            run, BuildStatus.fromString(run.getResult().toString()));

        MirrorGateResponse buildResponse = getMirrorGateService()
            .publishBuildData(builder.getBuildData());

        listener.getLogger().println("Check this project's builds results at: "
            + createMirrorgateLink(builder.getBuildData().getProjectName()));

        if (buildResponse.getResponseCode() == HttpStatus.SC_CREATED) {
            listener.getLogger().println("MirrorGate: Published Build "
                + "Complete Data. " + buildResponse.toString());
        } else {
            listener.getLogger().println("MirrorGate: Failed Publishing "
                + "Build Complete Data. " + buildResponse.toString());
        }

        sendBuildExtraData(builder, listener);

        LOG.fine("onCompleted ends");
    }

    protected MirrorGateService getMirrorGateService() {

        return service;
    }

    private String createMirrorgateLink(String projectName) {

        String mirrorgateUrl =
            HyperlinkNote.encodeTo(
                MirrorGateUtils.getMirrorGateAPIUrl() + "/dashboard.html?board=" + projectName,
                "MirrorGate");

        String image = ImgConsoleNote.encodeTo(MirrorGateUtils.getBase64image());

        return mirrorgateUrl + " " + image;
    }

    private void sendBuildExtraData(BuildBuilder builder, TaskListener listener) {

        List<String> extraUrl = MirrorGateUtils.getURLList();

        extraUrl.forEach(u -> {
            MirrorGateResponse response = getMirrorGateService()
                .sendBuildDataToExtraEndpoints(builder.getBuildData(), u);

            if (response.getResponseCode() != HttpStatus.SC_CREATED) {
                listener.getLogger().println("POST to " + u + " failed with code: "+response.getResponseCode());
            } else {
                listener.getLogger().println("POST to " + u + " succeeded!");
            }
        });
    }

}
