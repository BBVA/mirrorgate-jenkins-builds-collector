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

package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.console;

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.Symbol;

public class ImgConsoleNote extends ConsoleNote {

    private final String base64image;

    public ImgConsoleNote(String base64image) {
        this.base64image = base64image;
    }

    @Override
    public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
        String base64image = this.base64image;

        text.addMarkup(charPos, charPos, "<img src=" + base64image + ">", "</img>");
        return null;
    }

    protected String extraAttributes() {
        return "";
    }

    public static String encodeTo(String base64image) {
        try {
            return new ImgConsoleNote(base64image).encode();
        } catch (IOException e) {
            // impossible, but don't make this a fatal problem
            LOGGER.log(Level.WARNING, "Failed to serialize " + ImgConsoleNote.class, e);
            return base64image;
        }
    }

    @Extension
    @Symbol("hyperlink")
    public static class DescriptorImpl extends ConsoleAnnotationDescriptor {

        public String getDisplayName() {
            return "Hyperlinks";
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ImgConsoleNote.class.getName());

}
