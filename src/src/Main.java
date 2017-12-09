import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static int WHITE = 0;
    private static int RED = 1;
    private static int BLUE = 2;

    public static Automaton shrink (Automaton A, Collection<String> example) {
        Automaton result = null;
        float score = 0.0f;

        Set<State> allStates = A.getStates();

        for (State s : allStates) {
            s.setColour(WHITE);
        }

        for (State red : allStates) {
            red.setColour(RED);
            for (State blue : allStates) {
                if (!A.getInitialState().equals(blue) && !blue.equals(red)) {
                    blue.setColour(BLUE);
                    Automaton clone = A.clone();
                    State cloneRed = null, cloneBlue = null;

                    for (State s : clone.getStates()) {
                        if (s.getColour() == RED) cloneRed = s;
                        if (s.getColour() == BLUE) cloneBlue = s;
                    }

                    RPNI.merge(clone, cloneRed, cloneBlue);
                    float testScore = testAutomatonConsistency(A, clone, example);
                    if (testScore > score) {
                        result = clone;
                        score = testScore;
                    }
                    blue.setColour(WHITE);
                }
            }
            red.setColour(WHITE);

        }

        return result;
    }

    public static Automaton rShrink(Automaton A, Collection<String> example, int limit) {
        Automaton shrunk = shrink(A, example);
        if (shrunk.getStates().size() > limit)
            return rShrink(shrunk, example, limit);
        else
            return shrunk;
    }

    public static Automaton mostCons(Automaton A, Collection<String> example, int limit) {
        Automaton result = null;
        float maxConsistency = 0;
        Queue<Automaton> queue = new LinkedList<>();
        queue.add(A);
        while (!queue.isEmpty()) {
            Automaton current = queue.remove();
            if (current.getStates().size() <= limit) {
                float consistency = testAutomatonConsistency(A, current, example);
                if (consistency > maxConsistency) {
                    maxConsistency = consistency;
                    result = current;
                }
            } else {
                Set<State> allStates = current.getStates();

                for (State s : allStates) {
                    s.setColour(WHITE);
                }

                for (State red : allStates) {
                    red.setColour(RED);
                    for (State blue : allStates) {
                        if (!current.getInitialState().equals(blue) && !blue.equals(red)) {
                            blue.setColour(BLUE);
                            Automaton clone = current.clone();
                            State cloneRed = null, cloneBlue = null;

                            for (State s : clone.getStates()) {
                                if (s.getColour() == RED) cloneRed = s;
                                if (s.getColour() == BLUE) cloneBlue = s;
                            }

                            RPNI.merge(clone, cloneRed, cloneBlue);
                            queue.add(clone);
                            blue.setColour(WHITE);
                        }
                    }
                    red.setColour(WHITE);

                }
            }
        }

        return result;
    }

    private static float testAutomatonConsistency (Automaton origin, Automaton shrinked, Collection<String> example) {
        int consistent = 0;
        for (String s : example) {
            if (origin.run(s) == shrinked.run(s)) consistent++;
        }
        return (float) consistent / example.size() ;
    }

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(Option.builder("e")
                .argName("Filename")
                .desc("Use examples from file, otherwise use generated random examples")
                .hasArg(true)
                .optionalArg(true)
                .build());
        options.addOption("m", "Reduce DFA using most-cons instead of default R-Shrink");
        options.addOption(Option.builder("d")
                .argName("Filename")
                .desc("Save Graphviz Dot representation of reduced DFA")
                .hasArg(true)
                .optionalArg(true)
                .build());
        options.addOption(Option.builder("s")
                .argName("Filename")
                .desc("Save string representation of reduced DFA")
                .hasArg(true)
                .optionalArg(true)
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);
        List<String> argList = cmd.getArgList();

        Automaton automaton;
        int k;
        Set<String> examples;

        try {
            BufferedReader br = new BufferedReader(new FileReader(argList.get(0)));
            String automatonFile = br.lines().collect(Collectors.joining("\n"));
            br.close();

            BufferedWriter writer;
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String date = dateFormat.format(new Date());

            automaton = Parser.stringToAutomaton(automatonFile);
            k = Integer.valueOf(argList.get(1));

            if(cmd.hasOption("e")) {
                examples = new HashSet<>();
                BufferedReader br1 = new BufferedReader(new FileReader(cmd.getOptionValue("e")));
                String line = br1.readLine();
                while (line != null) {
                    if (line.matches("[01]+"))
                        examples.add(line);
                    line = br1.readLine();
                }
                br1.close();
            } else {
                examples = TestGenerator.generateExamples(30);
                writer = new BufferedWriter(new FileWriter(String.format("%s_example.txt", date)));
                for (String s : examples)
                    writer.write(s + "\n");
                writer.close();
            }

            Automaton result;

            if (cmd.hasOption("m"))
                result = mostCons(automaton, examples, k);
            else
                result = rShrink(automaton, examples, k);

            System.out.println(String.format("Consistency: %f", testAutomatonConsistency(automaton, result, examples)));

            if (cmd.hasOption("d")) {
                String dotFilename = cmd.getOptionValue("d");
                if (dotFilename == null)
                    dotFilename = String.format("%s_result.dot", date);
                writer = new BufferedWriter(new FileWriter(dotFilename));
                writer.write(result.toDot());
                writer.close();
            }

            if (cmd.hasOption("s")) {
                String stringFilename = cmd.getOptionValue("d");
                if (stringFilename == null)
                    stringFilename = String.format("%s_result.txt", date);
                writer = new BufferedWriter(new FileWriter(stringFilename));
                writer.write(Parser.automatonToString(result));
                writer.close();
            }

        } catch (Exception e) {
//            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "mergeDFA [Automaton File] [k States]", options );
        }

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "mergeDFA [Automaton File] [k States]", options );

        /**
         *  Remove below to Unit Test
         */

//        String strInput =
//                "s1 Y N 1-s2 0-s3 \n" +
//                        "s2 N N 1-s4 0-s5\n" +
//                        "s3 N Y \n" +
//                        "s4 N Y 1-s4\n" +
//                        "s5 N Y \n";
//
//        Automaton a = Parser.stringToAutomaton(strInput);
//        ArrayList<String> example = new ArrayList<>(Arrays.asList("0", "1", "00", "111", "10", "0111"));
//        BufferedWriter writer = new BufferedWriter(new FileWriter("test.dot"));
//        writer.write(a.toDot());
//        writer.close();
//
//        Automaton result = RShrink(a, example, 3);
//
//        BufferedWriter writer1 = new BufferedWriter(new FileWriter("result-rshrink.dot"));
//        writer1.write(result.toDot());
//        writer1.close();
//
//        Automaton result1 = MostCons(a, example, 3);
//
//        BufferedWriter writer2 = new BufferedWriter(new FileWriter("result-mostcons.dot"));
//        writer2.write(result1.toDot());
//        writer2.close();
//
//        System.out.println(TestAutomatonConsistency(a, result, example));
//        System.out.println(TestAutomatonConsistency(a, result1, example));
//
//        Automaton result1 = MostCons(a, example, 3);
//
//
//        Automaton result3 = TestGenerator.generateAutomaton(20);
//        BufferedWriter writer3 = new BufferedWriter(new FileWriter("result-PTA.dot"));
//        writer3.write(result3.toDot());
//        writer3.close();
//        System.out.println(result3.getNumberOfStates());
//        System.out.println(result3.getStates().size());
    }
}

