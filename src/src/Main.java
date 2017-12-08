import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class Main {

    public static void RPNIMerge (Automaton A, State red, State blue) {
        Set<State> allStates = A.getStates();

        if (red == null || blue == null)
            throw new NullPointerException();

        if (!allStates.contains(blue))
            throw new IllegalArgumentException();

        if (!allStates.contains(red))
            throw new IllegalArgumentException();

        if (A.getInitialState().equals(blue))
            throw new IllegalArgumentException();

        for (State state : allStates) {
            for (Transition transition : state.getTransitions()) {
                if (transition.getDest().equals(blue))
                    transition.setDest(red);
            }
        }

        RPNIFold(A, red, blue);

    }

    public static void RPNIFold (Automaton A, State red, State blue) {

        Set<State> allStates = A.getStates();
        //System.out.println(allStates.size());

        if (!allStates.contains(red))
            throw new IllegalArgumentException();

        if (blue.isAccept())
            red.setAccept(true);

        String chars = "01";

        for (char c : chars.toCharArray()) {
            if (blue.step(c) != null) {
                if (red.step(c) != null ) {
//
                    if(red.step(c).equals(blue) && blue.step(c).equals(red)) {

                        for(Transition t : red.getTransitions()) {
                            if (t.getMin() == c) {
                                t.setDest(red);
                            }
                        }
                    } else if (!red.step(c).equals(red)) {
                        RPNIFold(A, red.step(c), blue.step(c));
                    }
                } else {
//                    if (blue.step(c).equals(blue))
//                        red.addTransition(new Transition(c, red));
//                    else
                        red.addTransition(new Transition(c, blue.step(c)));
                }
            }
        }
    }

    static int WHITE = 0;
    static int RED = 1;
    static int BLUE = 2;

    public static Automaton Shrink (Automaton A, Collection<String> example) {
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

                    RPNIMerge(clone, cloneRed, cloneBlue);
                    float testScore = TestAutomatonConsistency(A, clone, example);
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

    public static Automaton RShrink(Automaton A, Collection<String> example, int limit) {
        Automaton shrunk = Shrink(A, example);
        if (shrunk.getStates().size() > limit)
            return RShrink(shrunk, example, limit);
        else
            return shrunk;
        
    }

    public static Automaton MostCons(Automaton A, Collection<String> example, int limit) {
        Automaton result = null;
        float maxConsistency = 0;
        Queue<Automaton> queue = new LinkedList<>();
        queue.add(A);
        while (!queue.isEmpty()) {
            Automaton current = queue.remove();
            if (current.getStates().size() <= limit) {
                float consistency = TestAutomatonConsistency(A, current, example);
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

                            RPNIMerge(clone, cloneRed, cloneBlue);
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

    private static float TestAutomatonConsistency (Automaton origin, Automaton shrinked, Collection<String> example) {
        int consistent = 0;
        for (String s : example) {
            if (origin.run(s) == shrinked.run(s)) consistent++;
        }
        return (float) consistent / example.size() ;
    }

    public static void main(String[] args) throws Exception {
        String strInput =
                    "s1 Y N 1-s2 0-s3 \n" +
                            "s2 N N 1-s4 0-s5\n" +
                            "s3 N Y \n" +
                            "s4 N Y 1-s4\n" +
                            "s5 N Y \n";

        Automaton a = Parser.stringToAutomaton(strInput);
        ArrayList<String> example = new ArrayList<>(Arrays.asList("0", "1", "00", "111", "10", "0111"));

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


        Automaton result3 = TestGenerator.GenerateAutomaton(20);
        BufferedWriter writer3 = new BufferedWriter(new FileWriter("result-PTA.dot"));
        writer3.write(result3.toDot());
        writer3.close();
        System.out.println(result3.getNumberOfStates());
        System.out.println(result3.getStates().size());
    }
}

