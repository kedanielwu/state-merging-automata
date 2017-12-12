import dk.brics.automaton.Automaton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {

    @Test
    public void TestSimpleStringToAutomaton () {
        String strInput =  "s1 Y N 1-s2 0-s3 \n" +
                "s2 N N 1-s4 0-s5\n" +
                "s3 N Y \n" +
                "s4 N Y 1-s4\n" +
                "s5 N Y \n";
        Automaton automaton = Parser.stringToAutomaton(strInput);

        assertEquals(5, automaton.getNumberOfStates());

        assertEquals(false, automaton.run("00"));
        assertEquals(false, automaton.run("100"));
        assertEquals(true, automaton.run("0"));
        assertEquals(true, automaton.run("10"));
        assertEquals(true, automaton.run("111"));
        assertEquals(true, automaton.run("1111"));
    }

    @Test
    public void TestAutomatonToString () {

        Automaton automaton = TestGenerator.generateAutomaton(20);

        String string = Parser.automatonToString(automaton);

        assertEquals(automaton.getNumberOfStates(), string.split("\n").length);
    }

}
