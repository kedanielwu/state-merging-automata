import dk.brics.automaton.Automaton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InferrerTest {

    Automaton automaton;
    Set<String> example;

    @BeforeEach
    void Setup() {
        ClassLoader classLoader = getClass().getClassLoader();
        File automatonFile = new File(classLoader.getResource("test1.txt").getFile());
        File exampleFile = new File(classLoader.getResource("test1_example.txt").getFile());

        String automatonString = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(automatonFile));
            automatonString = br.lines().collect(Collectors.joining("\n"));
            br.close();

            automaton = Parser.stringToAutomaton(automatonString);

            BufferedReader br2 = new BufferedReader(new FileReader(exampleFile));
            example = br2.lines().collect(Collectors.toSet());
            br.close();

        } catch (Exception e) {

        }
    }

    @Test
    void testRShrinkWithEmptyExample () {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Automaton result = Inferrer.rShrink(automaton, new HashSet<>(), 5);
                });
    }

    @Test
    void testRShrinkWithNegativeK () {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Automaton result = Inferrer.rShrink(automaton, example, -5);
                });
    }

    @Test
    void testRShrinkWithNullAutomaton () {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Automaton result = Inferrer.rShrink(null, example, 5);
                });
    }

    @Test
    void testMostConsWithEmptyExample () {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Automaton result = Inferrer.mostCons(automaton, new HashSet<>(), 5);
                });
    }

    @Test
    void testMostConsWithNegativeK () {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Automaton result = Inferrer.mostCons(automaton, example, -5);
                });
    }

    @Test
    void testMostConsWithNullAutomaton () {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Automaton result = Inferrer.mostCons(null, example, 5);
                });
    }

    @Test
    void testRShrinkWithEightStatesToFiveStates () {
        int k = 5;
        Automaton result = Inferrer.rShrink(automaton, example, k);

        assertTrue(result.getNumberOfStates() <= k);
    }

    @Test
    void testMostConsWithEightStatesToFiveStates () {
        int k = 5;
        Automaton mostConsResult = Inferrer.mostCons(automaton, example, k);
        Automaton rShrinkResult = Inferrer.rShrink(automaton, example, k);

        assertTrue(mostConsResult.getNumberOfStates() <= k);

        assertTrue(Inferrer.testAutomatonConsistency(automaton, mostConsResult, example)
                >= Inferrer.testAutomatonConsistency(automaton, rShrinkResult, example));
    }
}
