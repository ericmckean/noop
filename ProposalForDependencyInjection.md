Dependency injection is built into Noop. This means that any class may request injection of fields, and is guaranteed at runtime that instances of the class will be created by an injector. We provide some compile-time checks on the safety of the bindings, and make it easy to bind types to implementations. Following is a proposition to handle binding cleanly by a combination of pre-defined scopes, as well as the introduction of the `binding` keyword and a binding operator `->` to solve this problem.

# Concepts #
An **injection request** is a syntax used in declaring a class which indicates that some properties should be set by an injector. A class may have both injected and user-provided properties. Here is an example syntax:
`class Account[BankService service](Int initialBalance) {}`
The square brackets indicate that the injector should provide an appropriate instance of the BankService type, and set it as the initial value of a property on Account named service. The parentheses indicate that the initialBalance property cannot be set by the injector, rather, the code that creates the Account instance will provide the value.

A **TypeLiteral** is an expression that refers to a Type. For example, the String type can be referenced with the TypeLiteral `String`. This is roughly equivalent to `String.class` in Java for non-generic types. For generic types, the TypeLiteral represents the full type information, such as `List<String>` rather than a class literal which can only represent `List`. (note, noop doesn't have generic types yet)

A **binding** is a pair consisting of a TypeLiteral and an expression. The injector uses the binding so that when a request is made for injection of a particular type, the injector will know how to create an instance of that type. Noop has a binding operator, `->`, used to declare a binding.

Take a class defined as `MyClass(Apple a) {}`:
  * `Apple -> GreenApple` tells the injector to supply an instance of GreenApple.
  * `Apple -> myApple` tells the injector that the object referenced by myApple will be supplied.

Injection is requested whenever a new object is created. In Noop, **every** new object is created by the injector. So to create a new String object, you'd say `String s = String("hello")` and the injector provides the String instance. In this case, all of the information needed to construct the String has been provided, so the injector merely acts as a simple factory. In another case, `BankService s = BankServiceImpl()` provides no arguments to the constructor, so any parameters that appear in BankServiceImpl's constructor are requests for injection. Note that we don't support setter injection right now, but will probably need to do it for interoperability.

Sometimes a type is requested to be injected more than once in the course of executing the program. In this case, the injector might create a new instance, or it could inject the same instance that was used in a previous injection. This decision is governed by a **scope**, which groups a number of injection points together to share a single instance of the injected object.

Finally, we have **parent/child injectors**. When a program begins execution from the application entry point, Noop supplies a root injector, which is the common ancestor of all injectors. Whenever entering a method or block marked with the binding keyword, a new injector is created as a child of the current injector. When a request is made for injection, the child will supply it if possible, otherwise the injection is delegated to the parent. This allow bindings to be managed in components, so that two subcomponents cannot depend on each other's bindings, but can share bindings in their shared parent component.

# Builtin scopes #

## Singleton ##

This scope behaves as one would expect.  A binding in the singleton scope satisfies all dependents with the same instance.  Singletons should be stateless or thread-safe. This is also the default scope in Noop. If an expression is supplied for the binding, it will be evaluated only once, at the time it is first injected.

## ThreadLocal ##

This scope satisfies any dependents which execute together on the same thread. The semantics are similar to Java's ThreadLocal storage context.  In practice this is viable for multithreaded worker objects, and can provide an implementation of a Request scope for the web container scopes (see below).

## Unmanaged (no scope) ##

This scope, equivalent to Spring's prototype scope, produces a new object each time injection is requested, essentially caching nothing and managing nothing.  Unmanaged objects cannot participate in service lifecycles (start/stop) since they are unmanaged.  This is a dangerous scope, in that it looks like a scope, is used like a scope, but behaves entirely differently, turning a Provider into a Factory (in effect) and is mostly useful for value objects.

Note: This may not be implemented depending on the newable vs. injectable proposal outcomes.

## Web Context Scopes ##

For web-tiers, Noop may have an extension module which declares Session and Request scopes for use in J2EE containers (or which could be used in alternate web architectures).  These have proven valuable elsewhere and can simply be implemented by backing object unique caching in the Request or Session objects directly, as do most other containers.

## About the default scope ##

Dependency injection systems vary in what they consider their default scope.  Often they use "no scope" as a default, which is equivalent to spring's _prototype_ scope.  This can lead to some unclear situations, because these aren't dependencies on a managed object, they're dependencies on an unmanaged object, and this can lead to subtle ambiguities.

Noop defaults to the Singleton scope if otherwise unindicated.  This is both the most common case, and prevents naive mistakes around passing new copies of a type into dependents which will seem to work but subtly won't (since they're not talking to the same instance).  Such errors are caught earlier and can be explicitly set to `unmanaged` if appropriate.

# Declaring bindings #

The binding operator may be used anywhere in the code to introduce a new binding to the current injector.

The binding keyword is used to group a number of bindings for reuse, and to separate wiring from logic. It should be preferred over bindings made inline in code.

## Named binding ##
A named binding is a block of code defining bindings. It may be defined in a file by itself, which is similar to a Guice module, and is the preferred way. If desired, it may also be nested within a class.

```
// in MyBinding.noop
binding MyBinding {
  This -> That;
}

// in MyClass.noop
class MyClass {
  binding MyScope {
    MyInterface -> MyImplementation;
    Port -> 9876;
  }
}
```

The binding can then be referenced by name. It can be used on a method:

```
String helloWorld() binding LibraryOneBindings, LibraryTwoBindings {
 // some code
}
```

or in a block:

```
String helloWorld() {
  binding MyBinding {
    // all the objects bound in MyBinding may be injected here
  }
}
```

## Anonymous inline binding declarations ##

Note: this feature is somewhat troubling, as it prevents overriding by tests, and also mixes wiring with logic. It may be dropped.

When a binding is fairly simple it can be defined anonymously, just as the named binding it is usable on the method level or a block in the method:

```
String helloWorld() binding (MyInterface -> MyImplementation) {
 // some code
}
```

```
String helloWorld() {
  binding (MyInterface -> MyImplementation) {
    // all the objects created in that scope will use the bindings defined in MyScope
  }
}
```

# Injection in tests #
Speed is essential in unit testing, to allow the number of tests to grow very large without reducing their usefulness as a quick feedback tool. The root injector has different default bindings to support unit testing. When a test is executed, types are bound to a lightweight implementation. For example, File I/O objects like File are injected with Noop instances by default.