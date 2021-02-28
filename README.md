Evolutionary Algorithms for Kotlin
==================================

A poor man's (very) partial reimplementation of [DEAP](https://deap.readthedocs.io/en/master/) library, but in Kotlin... 
and not distributed.

What has been implemented
-------------------------

- Most of the main evolutionary algorithms.
- Rewrote the tree-based structure for Kotlin use.

What is different
-----------------

- Things are type safe. Individual and Fitness are templated.
- Because we can't dynamically add property to individuals (not easily at least), fitness is not a property of 
  individual, both are wrapped under `IndividualWithFitness`.
- Because objects are very hard to clone properly, every individual
  is assumed to be immutable.
  - This also means functions that mutates input in DEAP is converted
    to function that return the result.

License
-------

MIT