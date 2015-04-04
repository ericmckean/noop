# Introduction #

While other languages leave testing to third-party library authors, Noop builds in testing from the language syntax onward. Although test constructs are available in the language, third-party testing libraries are still supported and encouraged, so we intend to leave the door open for alternate test runners, assertion libraries, mocking libraries, etc.

We believe that test code looks different from production code. Tests should be very simple, with minimal conditional logic, so that they may be read as documentation. Each test should read like a little story of an object's life and interactions. We don't want to have type hierarchies, subtle behaviour, or lots of helper methods in a test.

Some awkward things happen when you add testing as a library in a language:
  * Associating a test with a class requires a naming convention, and only works for unit tests which map 1-1 with production classes
  * Refactoring tools don't know to rename the test when you rename a class, breaking that naming convention
  * must have a separate src-test root so tools know which sources to package for production, and be sure production sources don't depend on test sources
  * tests are methods with a special naming convention, and the condition being tested must be encoded into a valid method name, ie `testReaderShouldFindTheEndOfLine`
  * tests are grouped into classes, but any higher-level grouping requires a test suite class which lists the test classes to include
  * test runner doesn't easily know how to run a subset of tests within a test class
  * don't have any special syntax we can use in test code

# The `test` keyword #

A `test` is an entity, distinct from a class or method whose purpose is to run tests. It may be declared in a file with any name, which may live under the src-test source root of the codebase, or in the same source root with production sources. It is given a string name, meaning that the usual restrictions on identifier names don't apply. That is then followed by the test block.

```
// In src/test/noop/mypackage/MyImportantTests.noop
test "Look, ma! I'm a valid test name!" { ... }
```

Inside the test block, you can construct fixtures, execute code, and assert on the results. We provide a DSL for assertions, based on Hamcrest matchers.

```
test "try again" {
  -1.abs() should equal(1);
}
```

Here, the `should` keyword is a special operator, which evaluates the left-hand-side, then invokes the right-hand-side expression using a Hamcrest matcher.

Users may choose to use another test framework if they wish, by whatever mechanism that test framework uses. However, since Noop doesn't allow static methods, some of these API's will be hard to use. We will probably need to provide a way to call static methods on "legacy" Java code at some point.

# Grouping tests into suites #
This is a common feature of test frameworks. In most, tests are methods, which are grouped into a testcase, which is a class. Any higher-level grouping is done with a separate Suite class, or by discovery via metadata like an annotation.

In Noop, a suite is just another `test` block enclosing some tests, and may be arbitrarily nested, and divided among several files.

```
// In FastDateTests.noop
test "really fast tests" {
  test "fast date tests" {
    test "date can be added" {
      Date("2009-10-11") + 1.day() should equal(Date("2009-10-12"));
    }
  }
}
// In FastIntTests.noop
test "really fast tests" {
  test "some int tests" {
    test "my int tests" {
      test "thing" { 1.plus(2) should equal(3); }
    }
  }
}
```

In this example, the resulting structure is
> - Suite "really fast tests"
> > - Suite "fast date tests"
> > > - Test "date can be added"

> > - Suite "some int tests"
> > > - Suite "my int tests"
> > > > - Test "thing"

# Alternative: use tags instead of suites #
The problem with suites is that they statically define which groups of tests may be run together. If you have understandable tagging on tests, it may be easier to describe the group of tests you wish to run using tags, rather than having to use one of the provided suites. This allows the flexibility of a many-to-many mapping from tags to tests, which you can't have with suites.

# Replacing dependencies in tests #
This is what Noop is all about! To replace a dependency, use the `scope` keyword to create a child injector, and bind a different thing to the type you want to replace. For example,

```
// In MyClass.noop
class MyClass(Console c) {
  Void printThing() {
    c.println("thing");
  }
}
// In ATest.noop
test "Should print thing" {
  FakeConsole c = FakeConsole();
  scope (Console -> c) {
    MyClass(c).printThing();
  }
  c.getPrintedText() should equal("thing");
}
```

As this test runs, the Console type is bound to our FakeConsole instance, and within that scope, we create the class-under-test. The injector will provide our FakeConsole to satisfy the MyClass dependency, so we will be able to make assertions about what is printed.

# The `unittest` keyword #

A `unittest` is a special test where the fixture is assumed to be a single class. This allows the test code to refer to the class-under-test as "this", and avoid creating the fixture explicitly.

For this to work, the `unittest` may not be a root of the testsuite tree. It must appear beneath a test suite that defines a **single** type to be tested, or within the class itself (see the next section for details).

```
test "I'm testing MyClass again" scope (ClassUnderTest -> MyClass) {
  unittest "should print thing again" scope (Console -> FakeConsole) {
    printThing();
    c.asInstance(FakeConsole).getPrintedText() should equal("thing");
  }
}
```

# Writing unit-tests inline #
Since unittests are special and apply to a single class, they are allowed to appear inside a production source file. This allows them to act as a true "insider", with access to anything defined in the class, including private properties.

Here's an example:
```
import noop.Console;
import noop.FakeConsole;

class HelloWorld(Console c) {
  Int main(List args) {
    c.println("Hello tests!");
    return 1;
  }
  
  test "prints hello" scope (Console -> FakeConsole) {
    main(List()) should equal(1);
    c.asInstance(FakeConsole).getPrintedText() should contain("Hello tests!"));
  }
}

```

Notes:
  * The `import noop.FakeConsole` is allowed, and when running tests, this class will be available on the classpath. When running production code, the class is not available, but the import should be unused. If code outside the `test` block uses the FakeConsole, it will cause a runtime exception.
  * The cast of the Console to FakeConsole is annoying, but a necessary result of living inside the HelloWorld class where the c property is of type Console.

# How tests are handled by language tools #
In the interpreter, the `test` block is retained as a special construct, and executed when testing.

In the compiler, the behavior is governed by a flag, similar to the "debug" flag to many compilers. When the compiler is run in "production" mode, the test block is stripped from the emitted code. In "test" mode, it is retained, and converted to a normal class, or if nested, to a method in the host class.

Note that when tests appear in production code, the production code can be compiled independently of the tests, as long as you are not compiling in "test" mode - we lazy evaluate the imports and toss the test blocks. But, when you compile in test mode, both the production and test sources must be provided to the compiler in one step. Fortunately, our compiler knows about testing, and will allow a separate testSourceRoots parameter to declare where the tests are.

When the tests are compiled into classes and methods, we will need an appropriate strategy to name the class or method. If the target bytecode language doesn't allow spaces and other characters that we allow in test names, we'll have to escape them. We would still like to retain the original test name as metadata on the class or method, to display later.

# Behavioral Mocks #
Libraries like JMock are really convenient for providing an instance of a dependency that can assert how it is used. For example, a MockDatabase would be able to say if the correct thing was inserted.

## One way to do this... ##
Every test block allows expectations to be set, and mocks to be created, by delegating the test class to a !JMock Mockery, and asserting that the mockery is satisfied

```
test "a record is inserted into the database" {
  Database d = createMock(Database);
  checking(Expectations() {
    oneOf(d).insert("record"); will(returnValue("OK"));
  });
  MyClass(d).run();
}

```

# TODO #
Look at AtUnit http://code.google.com/p/atunit/
and Guiceberry http://code.google.com/p/guiceberry/
for good ideas

# Robertsdionne's Mock Syntax Brainstorming #

First, I'll assume we declare our methods like this (see Possible Method Syntax Twelve on ProposalForMultipleReturnValues):
```
class Foo() {

  doSomething(Int x, Int y) returns (Int a, Int b) {
    ...
  }
}
```

I'll also assume we have something akin to function pointers:
```
Foo foo = ...;

(Int, Int) returns (Int, Int) func = foo.doSomething;

(Int a, Int b) = func(5, 6); // just like calling foo.doSomething(5, 6);
```

And that we have closures, like this:
```
(Int x, Int y) returns (Int a, Int b) func = {
   ...
};

(Int a, Int b) = func(5, 6); // now has nothing to do with foo.doSomething
```

And now we get to expectation and mock behavior syntax:class MyClass(Foo foo) {

  behave(Int x, Int y) returns (Int) {
    (Int a, Int b) = foo.doSomething(x, y);
    return a * b;
  }
}

test "Test MyClass" {
  test "behave(x, y) returns doSomething(x,y).a * doSomething(x,y).b" {
    Foo foo = Foo.mock();
    MyClass myClass = MyClass.new(foo);

    foo.doSomething(null, null) returns (5, 6); // no expectation, just stubbed behavior

    assertEquals(30, myClass.behave(null, null)); // don't care about inputs
  }

  test "same as above but with expectations" {
    Foo foo = Foo.mock();
    MyClass myClass = MyClass.new(foo);

    expect foo.doSomething(2, 3) returns (55, 10); // this time we expect the call

    assertEquals(550, myClass.behave(2, 3));
  }
}
```