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
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpStatus;

@Extension
public class MirrorGateItemListener extends ItemListener {

    private static final Logger LOG = Logger.getLogger(MirrorGateItemListener.class.getName());

    private final MirrorGateService service;

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
                MirrorGateResponse buildResponse = publishBuild(builder.getBuildData());

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

