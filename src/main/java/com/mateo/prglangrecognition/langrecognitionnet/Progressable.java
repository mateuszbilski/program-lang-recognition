package com.mateo.prglangrecognition.langrecognitionnet;

public interface Progressable {
    public void setProgress(String name, double progressPercent);

    public void completed();
}
