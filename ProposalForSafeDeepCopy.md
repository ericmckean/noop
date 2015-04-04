# Introduction #

Without a built-in language-supported mechanism for convenient deep copying of input parameters, designs are doomed to leak state in unintended ways because not everybody thinks about such things at all times.  I propose NOOP should make input parameters deep copy by default, or perhaps not by default but readily available with a "copy" keyword with which to prefix input parameters.  I believe this is something C++ does right and Java does completely wrong.  Very similar to the mutable/immutable keyword, which may or may not be more effective than a deep copy mechanism.

# Details #

Example:
```
public class Foo(Bar bar, Baz baz) {

  public FooResult doSomethingDifficult(copy InputObject inputA, copy InputObject inputB);
}
```

Whether or not this should also be available to constructor parameters, I haven't yet considered.  The point of this is to supply a proven, correct and convenient method for deep copying objects when required so that programmers do not have to rely on some library somewhere that may or may not do exactly what they want with their particular objects.  Just like the mutable/immutable characterization proposed elsewhere, a deep copying mechanism makes code behavior more provable, and providing the keyword as an adjective for input parameters is good documentation.