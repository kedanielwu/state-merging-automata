import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.automaton.Transition;

import java.util.*;

public class Merger {

    public static void merge (Automaton A, State red, State blue) {
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

        for (Transition transition : blue.getTransitions()) {
            char c = transition.getMin();
            State dest = transition.getDest();
            red.addTransition(new Transition(c, dest));
        }

        if (blue.isAccept())
            red.setAccept(true);

        A.setDeterministic(false);
        A.determinize();

//        String chars = "01";
//
//        for (char c : chars.toCharArray()) {
//            Set<State> destSet = new HashSet<>();
//            red.step(c, destSet);
//            List<State> dests = new ArrayList<>(destSet);
//
//            while (dests.size() > 1) {
////                System.out.println("dests: "+dests);
////                System.out.println("dests size: "+dests.size());
//                merge(A, dests.get(0), dests.get(1));
//            }
//        }




//        fold(A, red, blue);

    }

    public static void fold (Automaton A, State red, State blue) {

        Set<State> allStates = A.getStates();

        if (!allStates.contains(red))
            throw new IllegalArgumentException();

        if (blue.isAccept())
            red.setAccept(true);

        String chars = "01";

        for (char c : chars.toCharArray()) {
            if (blue.step(c) != null) {
                
                if (blue.step(c).equals(blue)) {
                    State curr = red;
                    State next = red.step(c);
                    Set<State> visited = new HashSet<>();
                    while (!visited.contains(curr) && next != null) {
                        visited.add(curr);
                        curr = next;
                        next = next.step(c);
                    }
                    if (next == null)
                        curr.addTransition(new Transition(c, curr));
                    continue;
                }

                if (red.step(c) != null ) {
                    if(red.step(c).equals(blue) && blue.step(c).equals(red)) {

                        for(Transition t : red.getTransitions()) {
                            if (t.getMin() == c) {
                                t.setDest(red);
                            }
                        }
                    } else if (!red.step(c).equals(red)) {
                        System.out.println("red: " + red.hashCode());
                        System.out.println("red step: " + red.step(c).hashCode());
                        System.out.println("blue: " + blue.hashCode());
                        System.out.println("blue step: " + blue.step(c).hashCode());
                        fold(A, red.step(c), blue.step(c));
                    }
                } else {
                    red.addTransition(new Transition(c, blue.step(c)));
                }
            }
        }
    }
}
