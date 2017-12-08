import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.util.Set;

public class RPNI {

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

        fold(A, red, blue);

    }

    public static void fold (Automaton A, State red, State blue) {

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
                    if(red.step(c).equals(blue) && blue.step(c).equals(red)) {

                        for(Transition t : red.getTransitions()) {
                            if (t.getMin() == c) {
                                t.setDest(red);
                            }
                        }
                    } else if (!red.step(c).equals(red)) {
                        fold(A, red.step(c), blue.step(c));
                    }
                } else {
                    red.addTransition(new Transition(c, blue.step(c)));
                }
            }
        }
    }
}
