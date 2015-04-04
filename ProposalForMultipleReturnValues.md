# Introduction #

This is a really handy feature in some languages. Allow a syntax like:

```
class Foo() {
  String, String getThings() {
    return "a", "b"; 
    // or maybe just return a List<String>, but need to guarantee a matching size
  }
}


(String x, String y) = getThings();

```

In Python, there are tuples and lists, and tuples are just immutable lists (??). This is basically the same except we don't differentiate between tuples and immutable lists.


# Details #
This proposal is needed for the ProposalForErrors. Otherwise, it may not be worth doing, since it's not innovative.

The assignment operator = will need to understand how to assign to multiple values at once. Ideally, the method should return a fixed-size immutable list, and the caller needs to supply the same number of parameters to be assigned.

As an extension, we could allow an arbitrary-sized return list, and use a "tail" parameter to get the remaining elements:

```
String head;
List<String> tail;
(head, tail) = returnsManyStrings();
```

That's only performant for LinkedList's, though.

# Possible Method Syntaxes #

```
interface One {

  Int calculate(Int x);

  (Int a, Int b) calculate(Int x, Int y);
}
```

```
interface Two {

  Int calculate(Int x);

  Int a, Int b calculate(Int x, Int y);
}
```

```
interface Three {

  calculate(Int x) -> Int;

  calculate(Int x, Int y) -> (Int a, Int b);
}
```

```
interface Four {

  calculate(Int x) -> Int;

  calculate(Int x, Int y) -> Int a, Int b;
}
```

```
interface Five {

  calculate: Int x -> Int;

  calculate: Int x, Int y -> Int a, Int b;
}
```

```
interface Six {

  calculate: (Int x) -> (Int);

  calculate: (Int x, Int y) -> (Int a, Int b);
}
```

```
interface Seven {

  calculate(Int x, Int& a);

  calculate(Int x, Int y, Int& a, Int& b);
}
```

```
interface Eight {

  (Int) = calculate(Int x);

  (Int a, Int b) = calculate(Int x, Int y);
}
```

```
interface Nine {

  Int = calculate(Int x);

  Int a, Int b = calculate(Int x, Int y);
}
```

```
interface Ten {

  calculate(in Int x, out Int a);

  calculate(in Int x, in Int y, out Int a, out Int b);
}
```

```
interface Eleven {

  calculate: (Int x, Int y) returns (Int a, Int b) throws (Ex q, Ex r);
}

```

```
interface Twelve {

  calculate(Int x, Int y) returns (Int a, Int b) throws (Ex q, Ex r);
}
```

# Possible Return Syntaxes #

```
class One {

  Int a, Int b calculate(Int x, Int y) {
    return x, y;
  }
}
```

```
class Two {

  Int a, Int b calculate(Int x, Int y) {
    return (x, y);
  }
```

```
class Three {

  Int a, Int b calculate(Int x, Int y) {
    return a = x, b = y;
  }
}
```

```
class Four {

  Int a, Int b calculate(Int x, Int y) {
    return (a = x, b = y);
  }
}
```

```
class Five {

  Int a, Int b calcualte(Int x, Int y) {
    a = x, b = y;
  }

  // or with the & reference syntax
  calculate(Int x, Int y, Int& a, Int& b) {
    a = x, b = y;
  }

  // or with the in/out syntax
  calculate(in Int x, in Int y, out Int a, out Int b) {
    a = x, b = y;
  }
}
```

# Possible Return Value Assignment Syntaxes #

```
One one = ...;
Int a, Int b = one.calculate(5, 6);
```

```
Two two = ...;
(Int a, Int b) = two.calculate(5, 6);
```

```
Three three = ...;
Result result = three.calculate(5, 6);
assertEquals(5, result.a);
assertEquals(6, result.b);
```