import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.util.HashMap;
import java.util.Map;

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
        Map<State, Integer> stateName = new HashMap<>();
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

    public static void main(String[] args) {
        String strInput =
                    "s1 Y N 1-s2 0-s3 \n" +
                            "s2 N Y 1-s3 0-s4 \n" +
                            "s3 N N 0-s2 1-s4 \n" +
                            "s4 N Y \n";

        Automaton a = stringToAutomaton(strInput);


        String s = automatonToString(a);

        System.out.println(s);
    }
}
