# Fundamentals #
  * No primitive types, everything is an Object.
  * Strong typing
  * No optional syntax: semicolons and parenthesis required
  * Has properties
  * No facility for statics
  * Executable/compiled documentation as much as possible
  * Classes have retained metadata
  * There is always a seam between any pair of classes, for testing

# Readability #
  * Always be consistent
  * Don't use shortcuts or syntax sugar like underscores-for-variables or leaving off parentheses on method calls. These make it quicker to write, but harder to read, especially for a code reviewer who is not expert in the language but knows C++ or Java well.

# Good stdlib #
  * Pick best implementations from other languages
  * Use JodaTime for Date/Time apis
  * Use util.concurrent for concurrency
  * Expose Google collections
  * allow concurrent collection iteration when given a ConcurrentIterator?
  * Introspection done objective-c style (easy, few lines of boilerplate)

# Injection #
  * Use Guice or PicoContainer under the covers
    * Need to think hard about construction - can we use PicoContainer's greediest constructor? Is there always a default constructor?
    * Instead of greediest constructor approach, use optional injectables?
    * Need to examine scopes carefully.  Guice scopes can't be concentric without a lot of work.  Pico just uses parent containers/injectors for scope.  Guice can work that way... how do we want to do this?
  * A type is either newable or injectable, newable types cannot have injectable deps? See ProposalForNewableVsInjectable.
  * A first-class binding operator: `Service -> ServiceImpl`
  * Injectors/scopes as first-class language constructs

# Immutability #
  * `final` is the default, use the `mutable` keyword to make a variable which may change its reference.
  * implement const?
  * encourage functional style by having built-in predicate, filter, etc on collections, as well as convenient functions/blocks/closures/lambdas

# Strong inferred typing #
  * Avoid `null`, Types shouldn't allow null value by default
  * Maybe `Option<T>` to create a nullable T, like Scala. See NullObjectPatternProposal.
  * Inferred types? foo = "bar" means foo is a String

# Exceptions #
  * Only unchecked exceptions
    * Maybe a way to indicate errors to the caller aside from unchecked exception, see ProposalForErrors.

# Class parameters and properties #
  * class Foo(Bar bar) {} defines the default constructor, has a read-only property bar, like Scala. See ProposalForFirstClassProperties.

# Testing #
  * `test` keyword allows compact syntax for declaring nested test suites with tests as the leaves. No need to use classes and methods to declare test suites and tests. See ProposalForTestingApi.
  * Allow `test` blocks within production code, so the tests are right next to the code-under-test. Probably want to strip that from non-debug/non-test compiles.
  * tests have some "friend" relationship with the class under test, to allow whitebox test of private methods
  * Every instantiation goes through the injector, so there's always a seam between every pair of classes

# Public API #
  * need some way to enforce public API separate from visibility of types and methods. Maybe don't need "private" at all
  * Android has an interesting approach: create interface jars which have the implementations stripped from public classes and don't contain private stuff at all.

# Literals #
Lots of useful literals!
  * `String` is a type literal
  * `"Hello"` is a string literal
  * `"""line1\nline2"""` is a multi-line string literal
  * `/.*/` is a pattern literal
  * `{ "a": "b", "c": "d" }` is a hash literal (should evaluate to most specific type possible)
  * `[1,2]` is an array literal
  * ```http://google.com``` is a URI literal (like Fan, is it useful?)

# Optional to separate bindings from code #
Can we provide bindings to the injection framework inline, using scopes and @ImplementedBy and such? See PropositionForScopes.

# Enforce naming conventions #
This can help to improve readability, although we should take care to only enforce widely-agreed-upon conventions.
  * No reason the parser should allow types to start with lower case or variables to start with upper case