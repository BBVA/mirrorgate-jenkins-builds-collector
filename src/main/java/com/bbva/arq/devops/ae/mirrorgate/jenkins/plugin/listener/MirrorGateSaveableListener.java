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

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Job;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import java.util.logging.Logger;

@Extension
public class MirrorGateSaveableListener extends SaveableListener {

    private static final Logger LOG = Logger.getLogger(MirrorGateSaveableListener.class.getName());

    private final MirrorGateListenerHelper helper;

    public MirrorGateSaveableListener() {
        this.helper = new MirrorGateListenerHelper();

        LOG.fine(">>> MirrorGateSaveableListener Initialised");
    }

    @Override
    public void onChange(Saveable o, XmlFile file) {

        LOG.fine(">>> MirrorGateSaveableListener onChange starts");

        if (o instanceof Job) {
            Job job = (Job) o;
            if (!job.isBuildable()) {
                helper.sendBuildFromJob(job);
            }
        }

        super.onChange(o, file);

        LOG.fine(">>> MirrorGateSaveableListener onChange ends");
    }
}
