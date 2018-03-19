package com.mateo.prglangrecognition.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import static java.awt.BorderLayout.CENTER;

public class LogFrame extends JFrame {

    public LogFrame(int w, int h) {
        super("Log");
        setMinimumSize(new Dimension(w, h));
        scrollPane.setViewportView(textArea);
        add(scrollPane, CENTER);
        pack();
    }

    private final JScrollPane scrollPane = new JScrollPane();
    private final JFrame frame = new JFrame();
    private final JTextArea textArea = new JTextArea();
    private final TextAreaWriter writer = new TextAreaWriter(textArea);

    public JFrame getFrame() {
        return frame;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public TextAreaWriter getWriter() {
        return writer;
    }
}
