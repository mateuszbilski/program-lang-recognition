package com.mateo.prglangrecognition.gui;

import com.mateo.prglangrecognition.langrecognitionnet.Progressable;
import javax.swing.JLabel;

import static java.lang.String.format;

public class ProgressStatus implements Progressable {

    public ProgressStatus(JLabel statusBar) {
        this.statusBar = statusBar;
    }

    @Override
    public void setProgress(String name, double progressPercent) {
        statusBar.setText(format("%s: %.2f%% completed", name, progressPercent * 100));
    }

    @Override
    public void completed() {
        statusBar.setText("");
    }

    private final JLabel statusBar;
}
