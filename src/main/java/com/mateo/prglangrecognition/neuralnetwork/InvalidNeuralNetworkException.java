package com.mateo.prglangrecognition.neuralnetwork;

public class InvalidNeuralNetworkException extends Exception {

    public InvalidNeuralNetworkException() {
    }

    public InvalidNeuralNetworkException(String string) {
        super(string);
    }

    public InvalidNeuralNetworkException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public InvalidNeuralNetworkException(Throwable thrwbl) {
        super(thrwbl);
    }

}
