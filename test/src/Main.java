import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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



        if (!allStates.contains(blue)
                || !allStates.contains(red)
                || A.getInitialState().equals(blue))
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

        if (!allStates.contains(red))
            throw new IllegalArgumentException();

        if (blue.isAccept())
            red.setAccept(true);

        String chars = "01";

        for (char c : chars.toCharArray()) {
            if (blue.step(c) != null) {
                if (red.step(c) != null) {
                    RPNIFold(A, red.step(c), blue.step(c));
                } else {
                    red.addTransition(new Transition(c, blue.step(c)));
                }
            }
        }
    }

    public static void main(String[] args) {
        String strInput =
                    "s1 Y N 0-s2 1-s3 \n" +
                            "s2 N Y \n" +
                            "s3 N Y 1-s4 \n" +
                            "s4 N Y \n";

        Automaton a = stringToAutomaton(strInput);


        RPNIMerge(a, a.getInitialState().step('0'), a.getInitialState().step('1'));

        String s = automatonToString(a);

        System.out.println(a);

        System.out.println(s);

    }
}

