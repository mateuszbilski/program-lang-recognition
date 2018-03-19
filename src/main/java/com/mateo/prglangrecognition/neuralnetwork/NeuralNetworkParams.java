package com.mateo.prglangrecognition.neuralnetwork;

import java.io.Serializable;

public class NeuralNetworkParams implements Serializable {

    public NeuralNetworkParams(double learningFactor, double shapeFactor, double momentum, double bias) {
        this.learningRate = learningFactor;
        this.shapeFactor = shapeFactor;
        this.momentum = momentum;
        this.bias = bias;
    }

    public final double learningRate;

    public final double shapeFactor;

    public final double momentum;

    public final double bias;
}
