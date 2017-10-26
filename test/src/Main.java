import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class Main {
    public static void main(String[] args) {
        RegExp r = new RegExp("ab(c|d)*");
        Automaton a = r.toAutomaton();
        String s = "abcccdc";
        System.out.println("Match: " + a.run(s));
    }
}
