package com.mateo.prglangrecognition.neuralnetwork;

public class InvalidNeuralNetworkArgumentException extends Exception {

    public InvalidNeuralNetworkArgumentException() {
    }

    public InvalidNeuralNetworkArgumentException(String string) {
        super(string);
    }

    public InvalidNeuralNetworkArgumentException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public InvalidNeuralNetworkArgumentException(Throwable thrwbl) {
        super(thrwbl);
    }

}
