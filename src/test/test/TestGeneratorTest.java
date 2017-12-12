import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestGeneratorTest {
    @Test
    void testGenerateRandomExamplesWithNegativeCount() {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Set<String> examples = TestGenerator.generateRandomExamples(-20, 100);
                });
    }

    @Test
    void testGenerateRandomExamplesWithNegativeBound() {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Set<String> examples = TestGenerator.generateRandomExamples(20, -100);
                });
    }

    @Test
    void testGenerateRandomExamplesWithPositiveBoundAndCount() {
        int count = 20;
        int bound = 100;
        Set<String> examples = TestGenerator.generateRandomExamples(count, bound);
        assertEquals(count, examples.size());
        for (String s : examples)
            assertTrue(Integer.parseInt(s, 2) < bound);
    }

    @Test
    void testGenerateRandomExamplesWithOnlyPositiveBound () {
        int count = 20;
        Set<String> examples = TestGenerator.generateRandomExamples(count);
        assertEquals(count, examples.size());
    }

    @Test
    void testGenerateAcceptedExamplesFromAutomaton () {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("test1.txt").getFile());
        Automaton a = null;
        Set<String> examples = null;
        String automatonString = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            automatonString = br.lines().collect(Collectors.joining("\n"));
            br.close();

            a = Parser.stringToAutomaton(automatonString);
            examples = TestGenerator.generateAcceptedExamplesFromAutomaton(a, 500);

        } catch (Exception e) {

        }

        assertEquals(automatonString.split("\n").length, a.getNumberOfStates());

        for (String s : examples)
            assertTrue(a.run(s));
    }

    @Test
    void testGenerateAcceptedExamplesFromAutomatonWithNullAutomaton () {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Set<String> examples = TestGenerator.generateAcceptedExamplesFromAutomaton(null, 500);
                });
    }

    @Test
    void testGeneratePTAWithEmptyExampleSet () {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Automaton a = TestGenerator.generatePTA(new HashSet<>());
                });
    }

    @Test
    void testGeneratePTAWithNullExampleSet () {
        assertThrows(IllegalArgumentException.class,
                ()->{
                    Automaton a = TestGenerator.generatePTA(null);
                });
    }

    @Test
    void testGeneratePTAt () {

        Set<String> example = TestGenerator.generateRandomExamples(3);
        Automaton a = TestGenerator.generatePTA(example);

        for (String s : example)
            assertTrue(a.run(s));

        for (State state : a.getStates())
            assertTrue(state.getTransitions().size() <= 2);

    }
 }
