import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class Main {
    public static Automaton stringToAutomaton(String input) {
        //storage
        Map<String, State> states = new HashMap();
        //initial state
        String initial = null;
        //every line is a state, in the format
        //state, initial?, accept? transition1, transition2 ...
        //line split and state initialize
        for(String s: input.split("\n")){
            String[] tokens = s.split(" ");
            State temp = new State();
            states.put(tokens[0], temp);
            if (tokens[2].equals("Y")) {
                temp.setAccept(true);
            }
            if (tokens[1].equals("Y")) {
                initial = tokens[0];
            }
        }
        //add transition
        for (String s: input.split("\n")){
            String[] tokens = s.split(" ");
            State st = states.get(tokens[0]);
            for (int i=3; i<tokens.length; i++){
                String[] transString = tokens[i].split("-");
                Transition trans = new Transition(transString[0].charAt(0), states.get(transString[1]));
                st.addTransition(trans);
            }
        }
        //create DFA
        Automaton out = new Automaton();
        out.setInitialState(states.get(initial));
        System.out.println(out);
        return out;
    }

    public static String automatonToString (Automaton input) {

        StringBuilder sb = new StringBuilder();

        for (State state : input.getStates()) {
            String s = state.toString();
            String[] strings = s.split("\n");
            sb.append(strings[0].split(" ")[1] + " ");

            if(state.equals(input.getInitialState()))
                sb.append("Y ");
            else
                sb.append("N ");

            if(state.isAccept())
                sb.append("Y ");
            else
                sb.append("N ");

            for (int i = 1; i < strings.length; i++) {
                String transition = strings[i];
                transition = transition.trim().replace(" -> ", "-");
                sb.append(transition + " ");
            }
            sb.append("\n");
        }


        return sb.toString();
    }


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
                        System.out.println(red.hashCode() - red.step(c).hashCode());
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

        Automaton a = stringToAutomaton(strInput);
        ArrayList<String> example = new ArrayList<>(Arrays.asList("0", "1", "00", "111", "10", "0111"));

        BufferedWriter writer = new BufferedWriter(new FileWriter("test.dot"));
        writer.write(a.toDot());
        writer.close();

        Automaton result = Shrink(a, example);

        BufferedWriter writer1 = new BufferedWriter(new FileWriter("result.dot"));
        writer1.write(result.toDot());
        writer1.close();
    }
}

