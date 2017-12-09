# CSC410 Project: Inferring Automata using State-merging

## Required package

### dk.brics.automaton

[Use this one (mutable Transition class)](https://github.com/StivoEgg/dk.brics.automaton)

### Apache Common CLI

https://commons.apache.org/proper/commons-cli/index.htm

## Usage

```
Usage: mergeDFA [Automaton File] [k States]
Options:
 -d <Filename>   Save Graphviz Dot representation of reduced DFA
 -e <Filename>   Use examples from file, otherwise use generated random
                 examples
 -m              Reduce DFA using most-cons instead of default R-Shrink
 -s <Filename>   Save string representation of reduced DFA
 ```