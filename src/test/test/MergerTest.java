
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MergerTest {

    Automaton automaton;

    @BeforeEach
    void Setup() {
        ClassLoader classLoader = getClass().getClassLoader();
        File automatonFile = new File(classLoader.getResource("test1.txt").getFile());

        String automatonString = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(automatonFile));
            automatonString = br.lines().collect(Collectors.joining("\n"));
            br.close();

            automaton = Parser.stringToAutomaton(automatonString);


        } catch (Exception e) {

        }
    }

    @Test
    void testMergeWithNullBlueState() {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Merger.merge(automaton, automaton.getInitialState(), null);
                });
    }

    @Test
    void testMergeWithNullRedState() {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Merger.merge(automaton, null, automaton.getInitialState());
                });
    }

    @Test
    void testMergeWithSameRedBlueState() {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Merger.merge(automaton, automaton.getInitialState(), automaton.getInitialState());
                });
    }

    @Test
    void testMergeWithRedStateNotInAutomaton() {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Merger.merge(automaton, new State(), automaton.getInitialState());
                });
    }

    @Test
    void testMergeWithBlueStateNotInAutomaton() {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Merger.merge(automaton, automaton.getInitialState(), new State());
                });
    }

    @Test
    void testMerge() {
        State red = automaton.getInitialState();
        State blue = null;
        for (State state : automaton.getStates()) {
            if (!state.equals(red)) {
                blue = state;
                break;
            }
        }
        Merger.merge(automaton, red, blue);

        assertFalse(automaton.getStates().contains(blue));
        assertTrue(automaton.isDeterministic());
    }
}
