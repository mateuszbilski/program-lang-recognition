package com.mateo.prglangrecognition.gui;

import java.io.IOException;
import java.io.Writer;
import javax.swing.JTextArea;

public class TextAreaWriter extends Writer {

    public TextAreaWriter(JTextArea widget) {
        this.widget = widget;
    }

    @Override
    public void write(final char[] cbuf, final int off, final int len) {
        widget.append(new String(cbuf, off, len));
        widget.append("\n");
    }

    @Override
    public void write(final String str) {
        widget.append(str);
        widget.append("\n");
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    private final JTextArea widget;
}
