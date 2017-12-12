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

public class ComprehensiveTest {

    Automaton automaton;
    Set<String> example;


    void LoadTest(int i) {
        ClassLoader classLoader = getClass().getClassLoader();
        File automatonFile = new File(classLoader.getResource(String.format("test%d.txt", i)).getFile());
        File exampleFile = new File(classLoader.getResource(String.format("test%d_example.txt", i)).getFile());

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
    void testRShrinkWithEightStatesToFiveStates () {

        LoadTest(1);
        int k = 5;
        Automaton result = Inferrer.rShrink(automaton, example, k);

        assertTrue(result.getNumberOfStates() <= k);
    }

    @Test
    void testMostConsWithEightStatesToFiveStates () {
        LoadTest(1);
        int k = 5;
        Automaton mostConsResult = Inferrer.mostCons(automaton, example, k);
        Automaton rShrinkResult = Inferrer.rShrink(automaton, example, k);

        assertTrue(mostConsResult.getNumberOfStates() <= k);

        assertTrue(Inferrer.testAutomatonConsistency(automaton, mostConsResult, example)
                >= Inferrer.testAutomatonConsistency(automaton, rShrinkResult, example));
    }

    @Test
    void testRShrinkWithTwentyStatesPTAToTenStates () {

        LoadTest(2);
        int k = 10;
        Automaton result = Inferrer.rShrink(automaton, example, k);

        assertTrue(result.getNumberOfStates() <= k);
    }

    @Test
    void testMostConskWithTwentyStatesPTAToTenStates () {

        LoadTest(2);
        int k = 10;
        Automaton mostConsResult = Inferrer.mostCons(automaton, example, k);
        Automaton rShrinkResult = Inferrer.rShrink(automaton, example, k);

        assertTrue(mostConsResult.getNumberOfStates() <= k);

        assertTrue(Inferrer.testAutomatonConsistency(automaton, mostConsResult, example)
                >= Inferrer.testAutomatonConsistency(automaton, rShrinkResult, example));

    }

    @Test
    void testRShrinkWithSixteenStatesDFAToEightStates () {

        LoadTest(3);
        int k = 12;
        Automaton result = Inferrer.rShrink(automaton, example, k);

        assertTrue(result.getNumberOfStates() <= k);
    }

    @Test
    void testMostConskWithSixteenStatesDFAToEightStates () {

        LoadTest(3);
        int k = 12;
        Automaton mostConsResult = Inferrer.mostCons(automaton, example, k);
        Automaton rShrinkResult = Inferrer.rShrink(automaton, example, k);

        assertTrue(mostConsResult.getNumberOfStates() <= k);

        assertTrue(Inferrer.testAutomatonConsistency(automaton, mostConsResult, example)
                >= Inferrer.testAutomatonConsistency(automaton, rShrinkResult, example));

    }
}

