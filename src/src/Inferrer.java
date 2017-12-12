import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Inferrer {
    private static int WHITE = 0;
    private static int RED = 1;
    private static int BLUE = 2;

    private static Automaton shrink (Automaton A, Collection<String> example) {
        Automaton result = null;
        float score = 0.0f;
        int originalSize = A.getNumberOfStates();
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

                    Merger.merge(clone, cloneRed, cloneBlue);
                    float testScore = testAutomatonConsistency(A, clone, example);

                    if (testScore > score && clone.getNumberOfStates() <= originalSize) {
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

        if (example.isEmpty() || limit < 0 || A == null)
            throw new IllegalArgumentException();

        System.out.println(String.format("Current number of stages: %d", A.getNumberOfStates()));
        Automaton shrunk = shrink(A, example);
        if (shrunk.getStates().size() > limit)
            return rShrink(shrunk, example, limit);
        else {
            System.out.println(String.format(
                    "Consistency using R-Shrink: %f", Inferrer.testAutomatonConsistency(A, shrunk, example)));

            return shrunk;
        }
    }

    public static Automaton mostCons(Automaton A, Collection<String> example, int limit) {

        if (example.isEmpty() || limit < 0 || A == null)
            throw new IllegalArgumentException();

        Automaton result = null;
        float maxConsistency = 0;
        Queue<Automaton> queue = new LinkedList<>();
        queue.add(A);
        while (!queue.isEmpty()) {
            Automaton current = queue.remove();
            System.out.println(String.format("Current count in queue: %d", queue.size()));
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

                            Merger.merge(clone, cloneRed, cloneBlue);
                            if (clone.getNumberOfStates() < current.getNumberOfStates())
                                queue.add(clone);
                            blue.setColour(WHITE);
                        }
                    }
                    red.setColour(WHITE);

                }
            }
        }

        System.out.println(String.format(
                "Consistency using Most-Cons: %f", Inferrer.testAutomatonConsistency(A, result, example)));

        return result;
    }

    public static float testAutomatonConsistency (Automaton origin, Automaton shrinked, Collection<String> example) {
        int consistent = 0;
        for (String s : example) {
            if (origin.run(s) == shrinked.run(s)) consistent++;
        }
        return (float) consistent / example.size() ;
    }
}
