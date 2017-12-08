import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TestGenerator {
    private static Random random = new Random();
    private static final int MAX = 233333;


    public static Set<String> generateExamples(int count, int bound) {
        if (bound <= 0) throw new IllegalArgumentException();
        Set<String> examples = new HashSet<>();
        while (examples.size() < count) {
            String randS = Integer.toBinaryString(random.nextInt(bound));
            examples.add(randS.substring(1));
        }
        return examples;
    }

    public static Set<String> generateExamples(int count) {
        return generateExamples(count, MAX);
    }

    public static Automaton generatePTA (Collection<String> example) {
        Automaton result = new Automaton();
        State init = new State();
        result.setInitialState(init);
        for (String str : example) {
            State curr = init;
            State next = null;
            for (char c : str.toCharArray()) {
                next = curr.step(c);
                if (next == null) {
                    next = new State();
                    curr.addTransition(new Transition(c, next));
                }
                curr = next;
            }
            curr.setAccept(true);
        }


        return result;
    }

    public static Automaton generateAutomaton (int lowerbound) {
        Automaton result = new Automaton();
        Set<String> example = new HashSet<>();
        for (String s : generateExamples(lowerbound/2)) {
            example.add(s.substring(0, s.length()/2));
            example.add(s.substring(s.length()/2));
        }
        while (result.getStates().size() < lowerbound) {
            result = generatePTA(example);
        }
        return result;
    }
}
