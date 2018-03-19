package com.mateo.prglangrecognition.langrecognitionnet;

import com.mateo.prglangrecognition.langrecognitionnet.Parser.ParseResult;
import com.mateo.prglangrecognition.neuralnetwork.InvalidNeuralNetworkArgumentException;
import com.mateo.prglangrecognition.neuralnetwork.InvalidNeuralNetworkException;
import com.mateo.prglangrecognition.neuralnetwork.NeuralNetwork;
import com.mateo.prglangrecognition.neuralnetwork.NeuralNetworkParams;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import static java.lang.Character.isWhitespace;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.fill;
import static java.util.Collections.max;

public class LangRecognitionNet implements Serializable {

    private LangRecognitionNet() {

    }

    public static LangRecognitionNet loadScript(String scriptPath, Writer log, Progressable p) throws ScriptParsingException {

        LangRecognitionNet obj = new LangRecognitionNet();

        ArrayList<Integer> networkConf = new ArrayList<>();
        Scanner rd = null;
        ScriptState state = ScriptState.NETWORK_PARAMS;
        Integer checkNetworkAfterEpochs = 0;

        try {
            Set<String> symbolSet = new LinkedHashSet<>();
            Set<String> langSet = new LinkedHashSet<>();

            rd = new Scanner(new File(scriptPath), "utf8");

            boolean skipEmptyLines = true;
            while (rd.hasNextLine() && state != ScriptState.END_OF_PARSE) {

                String line = rd.nextLine().trim();

                if (!line.isEmpty() && line.charAt(0) == '#')
                    continue;
                else if (skipEmptyLines && line.isEmpty())
                    continue;

                if (line.length() >= 2 && line.charAt(0) == '\\' && line.charAt(1) == '#')
                    line = line.substring(1);

                switch (state) {
                    case NETWORK_PARAMS:
                        String[] params = line.split("\\s+");

                        int layerCount = params.length - 7;
                        if (layerCount < 0)
                            throw new ScriptParsingException("Too few arguments for network configuration");

                        for (int i = 1; i <= layerCount; i++)
                            networkConf.add(parseInt(params[i - 1]));

                        obj.networkParams = new NeuralNetworkParams(
                                parseDouble(params[params.length - 7]),
                                parseDouble(params[params.length - 6]),
                                parseDouble(params[params.length - 5]),
                                parseDouble(params[params.length - 4]));
                        obj.maxEpoch = parseInt(params[params.length - 3]);
                        obj.minError = parseDouble(params[params.length - 2]);
                        checkNetworkAfterEpochs = parseInt(params[params.length - 1]);

                        state = ScriptState.REGISTER_LANGUAGES;
                        skipEmptyLines = true;
                        break;

                    case REGISTER_LANGUAGES:
                        skipEmptyLines = false;
                        if (line.isEmpty()) {
                            state = ScriptState.SYMBOLS;
                            skipEmptyLines = true;
                            for (String langName : langSet) {
                                obj.learningPatternMap.put(langName, new ArrayList<String>());
                                obj.langList.add(langName);
                            }
                        } else {
                            String langName = line.split("\\s+")[0];
                            langSet.add(langName);
                        }
                        break;

                    case SYMBOLS:
                        skipEmptyLines = false;
                        if (line.isEmpty()) {
                            state = ScriptState.INPUT_PATTERN;
                            skipEmptyLines = true;
                        } else
                            symbolSet.addAll(asList(line.split("\\s+")));
                        break;

                    case INPUT_PATTERN:
                        skipEmptyLines = false;
                        if (line.isEmpty()) {
                            state = ScriptState.TEST_PATTERN;
                            skipEmptyLines = true;
                        } else {
                            int i = 0;
                            StringBuilder langBuilder = new StringBuilder();
                            while (!isWhitespace(line.charAt(i)))
                                langBuilder.append(line.charAt(i++));

                            while (isWhitespace(line.charAt(i)))
                                i++;

                            String language = langBuilder.toString(), fileName = line.substring(i);
                            List<String> fileNames = obj.learningPatternMap.get(language);
                            if (fileNames != null) {
                                fileNames.add(fileName);
                                obj.learningPatternMap.put(language, fileNames);
                                obj.inputPatternCount++;
                            } else
                                log.write("Undefined lang. " + language + " (" + fileName + ")");
                        }
                        break;

                    case TEST_PATTERN:
                        skipEmptyLines = false;
                        if (line.isEmpty()) {
                            state = ScriptState.END_OF_PARSE;
                            skipEmptyLines = true;
                        } else
                            obj.testPatterns.add(line);
                        break;
                }
            }

            if (!((state == ScriptState.INPUT_PATTERN && !skipEmptyLines) || state == ScriptState.TEST_PATTERN || state == ScriptState.END_OF_PARSE))
                throw new ScriptParsingException("Invalid script format");

            if (obj.inputPatternCount == 0)
                throw new ScriptParsingException("Empty input pattern list");
            else if (symbolSet.isEmpty())
                throw new ScriptParsingException("Empty operator set");

            obj.epochErrData = new ArrayList<>(obj.maxEpoch);
            //!Init Parse Class
            obj.parser = new Parser(symbolSet);

            //Creating neural network
            obj.inputCount = obj.parser.getSymbols().size();
            obj.outputCount = obj.langList.size();

            networkConf.add(0, obj.inputCount);
            networkConf.add(obj.outputCount);
            int networkLayers[] = new int[networkConf.size()];
            for (int i = 0; i < networkConf.size(); i++)
                networkLayers[i] = networkConf.get(i);

            obj.network = new NeuralNetwork(obj.inputCount, networkLayers, obj.networkParams);

            //Train neural network
            ArrayList<double[]> inputPatterns = new ArrayList<>();
            ArrayList<double[]> expectedOutputs = new ArrayList<>();

            int count = 0;
            for (String lang : obj.learningPatternMap.keySet()) {
                for (String fileName : obj.learningPatternMap.get(lang)) {

                    double[] expectedOutput = new double[obj.outputCount];
                    String containingPath = null;

                    if (!(new File(fileName).isAbsolute()))
                        containingPath = (new File(scriptPath)).getAbsoluteFile().getParent();
                    String inputPatternPath = (new File(containingPath, fileName)).getAbsolutePath();

                    try {
                        ParseResult result = obj.getParser().parseFile(inputPatternPath);

                        fill(expectedOutput, 0.0);
                        expectedOutput[obj.langList.indexOf(lang)] = 1.0;

                        inputPatterns.add(result.getPercentageOccurrences());
                        expectedOutputs.add(expectedOutput);

                        if (p != null)
                            p.setProgress("Parsing files", (++count) / (double) obj.inputPatternCount);
                    } catch (IOException ex) {
                        log.write(ex.getMessage() + "(" + inputPatternPath + ")");
                    }
                }
            }

            if (inputPatterns.isEmpty())
                throw new ScriptParsingException("None of files are accessible for training neural network");

            double errorLevel;
            int epochCount = 1, lastLog = 0;
            do {
                errorLevel = 0.0;
                for (int i = 0; i < inputPatterns.size(); i++)
                    errorLevel += obj.network.train(inputPatterns.get(i), expectedOutputs.get(i));
                errorLevel /= (double) obj.inputPatternCount;
                obj.epochErrData.add(errorLevel);

                if (p != null)
                    p.setProgress("Network training", (double) epochCount / obj.maxEpoch);

                if (log != null) {
                    if (checkNetworkAfterEpochs > 0 && epochCount % checkNetworkAfterEpochs == 0) {
                        lastLog = epochCount;
                        log.write(format("\nRecognizing after %d epochs (error level: %f)", epochCount, errorLevel));
                        obj.testRecognizingFiles(scriptPath, log);
                    }
                }

            } while (++epochCount <= obj.maxEpoch && errorLevel > obj.minError);

            if (p != null)
                p.completed();

            if (log != null && (epochCount - 1) != lastLog) {
                log.write(format("\nRecognizing after %d epochs (error level: %f)", epochCount - 1, errorLevel));
                obj.testRecognizingFiles(scriptPath, log);
            }
        } catch (IOException | InvalidNeuralNetworkException | NumberFormatException | InvalidNeuralNetworkArgumentException ex) {
            throw new ScriptParsingException(ex);
        } finally {
            if (rd != null)
                rd.close();
        }

        return obj;
    }

    public Map<String, Double> recognize(String path) throws LangRecognitionException {
        try {
            ParseResult result = parser.parseFile(path);
            double[] netResult = network.calculate(result.getPercentageOccurrences());

            Map<String, Double> recogResult = new LinkedHashMap<>();
            for (int i = 0; i < langList.size(); i++)
                recogResult.put(langList.get(i), netResult[i]);

            return recogResult;
        } catch (IOException | InvalidNeuralNetworkArgumentException ex) {
            throw new LangRecognitionException(ex);
        }
    }

    public Set<String> getSymbolList() {
        return parser.getSymbols();
    }

    public LinkedList<String> getLangList() {
        return langList;
    }

    public Map<String, List<String>> getLearningPatternMap() {
        return learningPatternMap;
    }

    public int getInputCount() {
        return inputCount;
    }

    public int getOutputCount() {
        return outputCount;
    }

    public NeuralNetwork getNetwork() {
        return network;
    }

    public NeuralNetworkParams getNetworkParams() {
        return networkParams;
    }

    public double getMaxEpoch() {
        return maxEpoch;
    }

    public Parser getParser() {
        return parser;
    }

    public List<Double> getEpochErrData() {
        return epochErrData;
    }

    private void testRecognizingFiles(String scriptPath, Writer log) {
        try {
            if (testPatterns.size() > 0 && log != null) {

                for (String item : testPatterns) {
                    try {
                        String containingPath = null;
                        if (!(new File(item).isAbsolute()))
                            containingPath = (new File(scriptPath)).getAbsoluteFile().getParent();
                        String inputPatternPath = (new File(containingPath, item)).getAbsolutePath();

                        Map<String, Double> result = recognize(inputPatternPath);
                        //find max value
                        Double maxValue = max(result.values());
                        String maxLang = "";
                        for (Entry<String, Double> itm : result.entrySet()) {
                            if (itm.getValue().equals(maxValue)) {
                                maxLang = itm.getKey();
                                break;
                            }
                        }

                        log.write(inputPatternPath + " as " + maxLang);
                    } catch (LangRecognitionException ex) {
                        log.write(ex.getMessage() + " (" + ex.getCause().getMessage() + " ) at " + item);
                    }
                }
            }
        } catch (IOException ex) {
        }
    }

    private enum ScriptState {
        NETWORK_PARAMS,
        SYMBOLS,
        REGISTER_LANGUAGES,
        INPUT_PATTERN,
        TEST_PATTERN,
        END_OF_PARSE
    }


    private final LinkedList<String> langList = new LinkedList<>();
    private Parser parser;

    private transient List<Double> epochErrData;
    private transient final Map<String, List<String>> learningPatternMap = new HashMap<>();
    private transient final List<String> testPatterns = new ArrayList<>();

    private int inputCount, outputCount, inputPatternCount;
    private NeuralNetwork network;
    private NeuralNetworkParams networkParams;
    private int maxEpoch;
    private double minError;
}
