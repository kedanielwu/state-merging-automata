import dk.brics.automaton.Automaton;
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



    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(Option.builder("e")
                .argName("Filename")
                .desc("Use examples from file, otherwise use generated random examples")
                .hasArg(true)
                .optionalArg(true)
                .build());
        options.addOption("m", "Reduce DFA using most-cons instead of default R-Shrink");
        options.addOption("r", "Output each step in R-shrink");
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

        int k;
        Set<String> examples;
        Automaton automaton;

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

               Set<String> pos = TestGenerator.generateAcceptedExamplesFromAutomaton(automaton, 1500);
               examples = TestGenerator.generateRandomExamples((pos.size()>1500)?pos.size():1500);
               System.out.println(pos.size());
               examples.addAll(pos);
               writer = new BufferedWriter(new FileWriter(String.format("%s_example.txt", date)));
               for (String s : examples)
                   writer.write(s + "\n");
               writer.close();
           }

            Automaton result;
            System.out.println(String.format("Target number of stages: %d", k));

            if (cmd.hasOption("m"))
                result = Inferrer.mostCons(automaton, examples, k);
            else {

                if (cmd.hasOption("r"))
                    result = Inferrer.rShrinkOutput(automaton, examples, k);
                else
                    result = Inferrer.rShrink(automaton, examples, k);
            }


            if (result == null)
                throw new IllegalStateException("Fail");


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
           System.out.println(e.getMessage());
           HelpFormatter formatter = new HelpFormatter();
           formatter.printHelp( "mergeDFA [Automaton File] [k States]", options );
       }

    }
}

