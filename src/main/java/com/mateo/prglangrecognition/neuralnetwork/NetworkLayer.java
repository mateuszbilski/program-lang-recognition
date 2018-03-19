package com.mateo.prglangrecognition.neuralnetwork;

import java.io.Serializable;

import static java.lang.Math.exp;

public class NetworkLayer implements Serializable {

    NetworkLayer(int neuronsCount, int inputCount) {
        this.neuronsCount = neuronsCount;
        this.inputCount = inputCount;
        neurons = new Neuron[neuronsCount];
        for (int i = 0; i < neuronsCount; i++)
            neurons[i] = new Neuron(inputCount);
    }

    void calculate(double[] input, NeuralNetworkParams param) {
        for (int n = 0; n < neuronsCount; n++) {
            double sum = 0.0;
            for (int i = 0; i < neurons[n].inputCount; i++) {
                sum += input[i] * neurons[n].weights[i];
            }
            sum += neurons[n].biasWeight * param.bias;

            neurons[n].output = sigmoidFunction(sum, param.shapeFactor);
        }
    }

    double[] getOutput() {
        double[] tab = new double[neuronsCount];
        for (int i = 0; i < neuronsCount; i++) {
            tab[i] = neurons[i].output;
        }
        return tab;
    }

    void updateNeuronsWeights(double[] prevLayerOutput, double[] nextLayerDelta, NeuralNetworkParams param) {
        for (int i = 0; i < neuronsCount; i++) {
            for (int j = 0; j < neurons[i].inputCount; j++) {
                double factor = param.learningRate * prevLayerOutput[j] * nextLayerDelta[i] + param.momentum * neurons[i].prevDelta[j];
                neurons[i].weights[j] = neurons[i].weights[j] - factor;
                neurons[i].prevDelta[j] = factor;
            }

            double factor = param.learningRate * param.bias * nextLayerDelta[i] + param.momentum * neurons[i].biasPrevDelta;
            neurons[i].biasWeight = neurons[i].biasWeight - factor;
            neurons[i].biasWeight = factor;
        }
    }

    double sigmoidFunction(double x, double betaFactor) {
        return (1.0 / (1.0 + exp(-betaFactor * x)));
    }

    final int neuronsCount;
    final int inputCount;
    Neuron[] neurons;
}