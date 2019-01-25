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
package hudson.console;

import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MirrorGateHyperlinkNote extends HyperlinkNote {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ModelHyperlinkNote.class.getName());

    public MirrorGateHyperlinkNote(String url, int length) {
        super(url, length);
    }

    @Override
    protected String extraAttributes() {
        return "target='_blank'";
    }

    public static String encodeTo(String url, String text) {
        return MirrorGateHyperlinkNote.encodeTo(url, text, MirrorGateHyperlinkNote::new);
    }

    @Restricted(NoExternalUse.class)
    static String encodeTo(String url, String text, BiFunction<String, Integer, ConsoleNote> constructor) {
        // If text contains newlines, then its stored length will not match its length when being
        // displayed, since the display length will only include text up to the first newline,
        // which will cause an IndexOutOfBoundsException in MarkupText#rangeCheck when
        // ConsoleAnnotationOutputStream converts the note into markup. That stream treats '\n' as
        // the sole end-of-line marker on all platforms, so we ignore '\r' because it will not
        // break the conversion.
        text = text.replace('\n', ' ');
        try {
            return constructor.apply(url,text.length()).encode()+text;
        } catch (IOException e) {
            // impossible, but don't make this a fatal problem
            LOGGER.log(Level.WARNING, "Failed to serialize "+HyperlinkNote.class,e);
            return text;
        }
    }

    @Extension
    @Symbol("MirrorGateHyperLink")
    public static class DescriptorImpl extends HyperlinkNote.DescriptorImpl {
        @Nonnull
        public String getDisplayName() {
            return "MirrorGateHyperLink";
        }
    }

}
