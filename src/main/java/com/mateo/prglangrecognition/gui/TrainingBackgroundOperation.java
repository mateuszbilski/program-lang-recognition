package com.mateo.prglangrecognition.gui;

import com.mateo.prglangrecognition.langrecognitionnet.LangRecognitionNet;
import com.mateo.prglangrecognition.langrecognitionnet.Progressable;
import com.mateo.prglangrecognition.langrecognitionnet.ScriptParsingException;
import java.io.IOException;
import java.io.Writer;

import static com.mateo.prglangrecognition.langrecognitionnet.LangRecognitionNet.loadScript;

public class TrainingBackgroundOperation implements Runnable {

    public interface Callbackable {
        public void doCallback(TrainingBackgroundOperation obj);
    }

    public TrainingBackgroundOperation(String scriptFile, Writer writer, Progressable p, Callbackable c) {
        this.scriptFile = scriptFile;
        this.writer = writer;
        this.p = p;
        this.c = c;
    }

    @Override
    public void run() {
        try {
            writer.write("\nStarting training: " + scriptFile);
            recog = loadScript(scriptFile, writer, p);
            writer.write("\nTraining completed");
        } catch (ScriptParsingException ex) {
            Throwable cause = ex.getCause();
            String message = ex.getMessage() + (cause != null ? " (Cause: " + cause.getMessage() + ")" : "");
            try {
                writer.write(message);
            } catch (IOException expection) {
            }
            this.ex = ex;
        } catch (IOException ex) {
        } finally {
            c.doCallback(this);
        }
    }

    public String getScriptFile() {
        return scriptFile;
    }

    public Writer getWriter() {
        return writer;
    }

    public Progressable getP() {
        return p;
    }

    public LangRecognitionNet getRecog() {
        return recog;
    }

    public Exception getEx() {
        return ex;
    }


    private Callbackable c;
    private String scriptFile;
    private Writer writer;
    private Progressable p;
    private LangRecognitionNet recog;
    private Exception ex;
}
