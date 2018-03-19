package com.mateo.prglangrecognition.neuralnetwork;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

import static java.util.Arrays.fill;

class Neuron implements Serializable {

    Neuron(int inputCount) {
        this.inputCount = inputCount;
        Random rand = new Random();
        weights = new double[inputCount];
        prevDelta = new double[inputCount];
        fill(prevDelta, 0.0);

        for (int i = 0; i < inputCount; i++) {
            weights[i] = (rand.nextDouble() - 0.5) * RANDOM_FACTOR;
        }
    }

    double biasWeight;
    double biasPrevDelta;
    double output;
    double[] weights;
    double[] prevDelta;
    final int inputCount;
    private static final double RANDOM_FACTOR = 0.9;
}
