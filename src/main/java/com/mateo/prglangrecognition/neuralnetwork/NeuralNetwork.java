package com.mateo.prglangrecognition.neuralnetwork;

import java.io.Serializable;

import static java.lang.Math.pow;

public class NeuralNetwork implements Serializable {

    public NeuralNetwork(int inputCount, int[] networkConf, NeuralNetworkParams p) throws InvalidNeuralNetworkException {
        this.p = p;
        if (networkConf.length < 2)
            throw new InvalidNeuralNetworkException("Network must have two or more layers");
        layers = new NetworkLayer[networkConf.length];
        layers[0] = new NetworkLayer(networkConf[0], inputCount);
        for (int i = 1; i < layers.length; i++) {
            layers[i] = new NetworkLayer(networkConf[i], layers[i - 1].neuronsCount);
        }
    }

    public double train(double[] input, double[] exceptedOutput) throws InvalidNeuralNetworkArgumentException {
        double quadraticError = 0.0;
        double[] output = calculate(input);

        if (output.length != exceptedOutput.length)
            throw new InvalidNeuralNetworkArgumentException("Output and excepted output mismatch");

        for (int i = 0; i < output.length; i++)
            quadraticError += pow((exceptedOutput[i] - output[i]), 2.0);

        //output layer
        double[] delta = new double[layers[layers.length - 1].neuronsCount];
        for (int i = 0; i < delta.length; i++)
            delta[i] = (1 - layers[layers.length - 1].neurons[i].output) * layers[layers.length - 1].neurons[i].output *
                    (layers[layers.length - 1].neurons[i].output - exceptedOutput[i]);
        layers[layers.length - 1].updateNeuronsWeights(layers[layers.length - 2].getOutput(), delta, p);

        //hidden layer
        for (int i = layers.length - 2; i >= 0; i--) {
            delta = calculateDeltaInLayer(i, delta);
            if (i != 0)
                layers[i].updateNeuronsWeights(layers[i - 1].getOutput(), delta, p);
            else
                layers[i].updateNeuronsWeights(input, delta, p);
        }

        return quadraticError / 2.0;
    }

    private double[] calculateDeltaInLayer(int layerNumber, double[] nextLayerDelta) {
        if (layerNumber == layers.length - 1)
            throw new RuntimeException("Method cannot calculate delta in output layer");

        double[] delta = new double[layers[layerNumber].neuronsCount];
        for (int i = 0; i < delta.length; i++) {
            double sum = 0.0;
            for (int k = 0; k < layers[layerNumber + 1].neuronsCount; k++)
                sum += nextLayerDelta[k] * layers[layerNumber + 1].neurons[k].weights[i];

            delta[i] = sum * layers[layerNumber].neurons[i].output *
                    (1 - layers[layerNumber].neurons[i].output);
        }

        return delta;
    }

    public double[] calculate(double[] input) throws InvalidNeuralNetworkArgumentException {
        if (layers[0].inputCount != input.length)
            throw new InvalidNeuralNetworkArgumentException("Input mismatch");

        layers[0].calculate(input, p);
        for (int i = 1; i < layers.length; i++)
            layers[i].calculate(layers[i - 1].getOutput(), p);
        return layers[layers.length - 1].getOutput();
    }

    private NetworkLayer[] layers;
    private NeuralNetworkParams p;
}
