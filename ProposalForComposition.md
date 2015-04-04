# Introduction #

Inheritance is often misused. For example, many web frameworks require subtypes of HttpServlet all over the place, which forces you to deal with the constructor of HttpServlet to test your classes.

It's also bad because it breaks encapsulation of the superclass, so the author of the subclass can run into lots of problems. (TODO: examples from Effective Java) The protected API can have extra thorns that the public API doesn't - although you can still break things even with composition using the public API.

The general guidance here is Favor Composition over Inheritance. I think the language can make this the only way. Our goal is to provide the same subtyping advantage of inheritance without coupling the particular supertype.

# Details #

The real value of a subtype is polymorphism.

```
Animal getAnimal() {
  return new Cat();
}

Animal c = new Cat();
```

Ok, so you decide to create a Dog class. You need to be able to assign an Animal reference to point to an instance of Dog. But, we don't want you to subclass Animal.

Instead, use composition, and delegate to an instance of Animal.

```
class Dog(Animal delegate) {
  String name() {
    return delegate.name();
  }

  @Override
  Int numberOfLegs() {
    return 4;
  }
}
Animal dog = new Dog(); // doesn't work!!!
```

The problem with plain composition is that Dog is not an instance of Animal. The answer in Java is to have an Interface as the common ancestor of both. But if Animal is a class outside your control, you're in trouble, as you cannot make it implement a new interface.

Another problem is if the delegate has 30 public methods, then you have a long class that just delegates all 30 methods, minus whatever different behavior you want. You're required to have all of them, to honor the polymorphism.

So, here's a possible syntax:

```
class Dog(Animal delegate) composes delegate {
  Int numberOfLegs() {
    return 4;
  }
}
Animal dog = new Dog(); // works!
```

The composes keyword indicates that this class is now a subtype of Animal, however, we didn't inherit from Animal. And all the public methods of Animal are now implemented in Dog for free as straight pass-throughs. We can then override them as we like, and use the delegate reference. The delegate is provided like any other constructor parameter.

We essentially did this:
  * created an anonymous interface that matches the public API of Animal
  * made Animal implement that interface, it already has the implementation
  * made Dog implement that interface with a bunch of delegation
  * client code still sees Animal as a concrete type
  * method dispatch on an Animal reference still checks for polymorphic subtypes

As the best advantage, take the HttpServlet example:

```
class MyServlet(HttpServlet hs) {
}

MyServlet forTesting = new MyServlet(new MockHttpServlet());
```

# Alternate Syntax #
I'm liking this way more and more:
```
class Circle(Int i) => Shape s {
  
}

Shape delegate = new Shape();
Shape c = new Circle(i) => delegate;
```

# Strong Delegate concept #
an alternative is to use a delegate notion with strong language support.  Essentially the keyword "delegate" provides the compiler with a hint to expand the API of the class to include the methods on the delegate, and the delegating class can be passed around as the type of the delegate (without having to formally declare its interface).  For example:

```

public class Foo {
  public String doFoo() { return "foo"; }
}

public class Delegator(delegate Foo foo) {}

// calling code can then do the following
...
  Foo foo = new Foo();
  Delegator d = new Delegator(foo);
  assertEquals("foo", d.doFoo());
// and Delegator is assignable to a Foo variable
  Foo otherFoo = d;

```

The class is free to implement it's own form of the method, and can always call the delegate's implementation directly like so:

```

public class Foo {
  public String doFoo() { return "foo"; }
}

public class Delegator(delegate Foo foo) {

  public String doFoo() { return foo.doFoo() + "blah"; }

}

// calling code can then do the following
...
  Foo foo = new Foo();
  Delegator d = new Delegator(foo);
  assertEquals("fooblah", d.doFoo());
```

This can be done with multiple delegates, which would each contribute their API to the delegating object.  In the case where the two or more delegates have no common API, there is no ambiguity, and the obvious semantic applies (methods on Foo go to that delegate, methods on Bar go to that delegate).  However, if there is a union between the set of methods/properties comprising the API, then the common methods would need to be disambiguated by means of a method delegation syntax.  The following example would not compile:

```
public class Foo {
  public String doFoo() { return "foo"; }
  public String doCommon() { return "commonFoo"; }
}
public class Bar {
  public String doBar() { return "bar"; }
  public String doCommon() { return "commonBar"; }
}

public class Delegator(delegate Foo foo, delegate Bar bar) {}
```

This would require disambiguation.  The compiler would disallow it until the following hint was provided:
```
public class Delegator(delegate Foo foo, delegate Bar bar) {
  public String doCommon() delegate foo;
}

// The calling code would therefore invoke doCommon on foo
  Foo foo = new Foo();
  Bar bar = new Bar();
  Delegator d = new Delegator(foo, bar);
  assertEquals("commonFoo", d.doCommon());
```

Note that this is instance delegation, not implementation inheritance.  So two delegates of the same type could be provided like so:

```
public class Foo(String crazy) {
  public String processCrazy() { return crazy; } 
  public String toUpperCase() { return crazy.toUpperCase(); }
}

public class Delegator(delegate Foo foo1, delegate Foo foo2) {
  public String processCrazy() delegate foo1;
  public String toUpperCase() delegate foo2;
}

...
  Foo foo1 = new Foo("Foo1");
  Foo foo2 = new Foo("Foo2");
  Delegator d = new Delegator(foo1, foo2);
  assertEquals("Foo1", d.processCrazy());
  assertEquals("FOO2", d.processCrazy());
```

This can be used to simulate inheritance, but is specifically instance-based, and can therefore be used to implement delegate patterns using extremely terse form, and with minimal boilerplate.

Sometimes the user might want to expose a small subset of methods belonging to a member object as methods of the containing object without simulating inheritance.  One solution might be to allow using the above "delgateto" syntax on injected objects that are not qualified with the "delegate" keyword.  See below:

```
public class Foo {

  public void doOneThing() { /*...*/ }

  public void doAnotherThing() { /*...*/ }

  public void doManyMoreThings() { /*...*/ }
}

public class Delegator(Foo foo) { // notice the "delegate" keyword is missing from the injected parameter list

  public void doOneThing() delegate foo;
}

// Delegator forwards calls to doOneThing() to its foo member.  Delegator is not a Foo.
Foo foo = new Foo();
Delegator d = new Delegator(foo);
d.doOneThing(); // invokes foo.doOneThing()

d.doAnotherThing(); // compiler error
d.doManyMoreThings(); // compiler error
Foo otherFoo = d; // compiler error
```

Thus, the "delegateto" method syntax could remove boilerplate code for simple wrappers that only expose proper subsets of the wrapped object's public methods.

# Modification to delegate keyword to allow for polymorphic state machines #

```
// Type definitions
interface Calculator {
  Double calculate(Double left, Double right);
}

class Add() implements Calculator {
  Double calculate(Double left, Double right) {
    return left + right;
  }
}

class Subtract() implements Calculator {
  Double calculate(Double left, Double right) {
    return left - right;
  }
}

class Multiply() implements Calculator {
  Double calculate(Double left, Double right) {
    return left * right;
  }
}

class Divide() implements Calculator {
  Double calculate(Double left, Double right) {
    return left / right;
  }
}

enum State {
  ADD,
  SUBTRACT,
  MULTIPLY,
  DIVIDE
}

class StatefulCalculator(
    Calculator begin as delegate state, Map<String, Calculator> space) {
  void setMode(State mode) {
    state = space.get(mode);
  }
}

// Usage
Map<String, Calculator> space = HashMap.new();
Calculator begin = Add.new();
space.put(State.ADD, begin);
space.put(State.SUBTRACT, Subtract.new());
space.put(State.MULTIPLY, Multiply.new());
space.put(State.DIVIDE, Divide.new());

StatefulCalculator calculator = new StatefulCalculator(begin, space);

assertEquals(4, calculator.calculate(2, 2));

calculator.setMode(State.SUBTRACT);
assertEquals(0, calculator.calculate(2, 2));

calculator.setMode(State.MULTIPLY);
assertEquals(6, calculator.calculate(2, 3));

calculator.setMode(State.DIVIDE);
assertEquals(5, calculator.calculate(10, 2));

// Also
Calculator restrictedView = calculator;
restrictedView.calculate(5, 6) // polymorphic depending upon the state of the StatefulCalculator

// The above example is very simple, but essentially you could pass an arbitrary directed
// graph in the map representing the state space, and have state transitions depend upon
// input combined with the current hidden state.

// I feel that this pattern, enabled by the new "as delegate" keyword syntax, will promote
// polymorphism over switch/case/if statement hell. -robertsdionne
```