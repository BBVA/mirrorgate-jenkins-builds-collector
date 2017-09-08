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
