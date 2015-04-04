# Introduction #

This feature is motivated by Misko's blog post, To new or not to new (http://misko.hevery.com/2008/09/30/to-new-or-not-to-new/)

Our proposal in Noop is that a class should ideally either be:
  * a "service", or "injectable", constructed entirely from things the injector knows how to provide, and then provided by the injector or
  * a "data", or "newable", made from objects that another class should provide at runtime, and not provided by the injector

Mixing them is problematic, but necessary.

## When a data needs a service ##
For example, a Range object may want a Logger, which should be provided by the injector, yet the initial values of the range are quite newable. And requesting injection of the Range type doesn't make sense, unless it is aliased. Here is an example syntax, which uses different brackets to denote the injectable properties from the newable ones:
```
class Range[Logger logger](Int start, Int end) {}
...
Range r = Range(1,3);
```

This is conceptually similar to a factory, which can be created by a DI framework like Spring or Guice, and then you'd call a create method passing the newable parts, so that the product of the factory may have both newable and injectable dependencies. It's also similar to Assisted Injection in Guice2.

## When a data becomes a service ##
The String type would usually be newable. However, when aliased to Username, it's natural that a request-scoped injector could provide an instance. The alias itself provides the clue: String -> Username is a good indication that the newable type String is aliased to an injectable type Username.

# Alternate proposal with named parameters #

It's troubling that the square/round paren syntax forces you to decide how your class will be created later. Maybe you think that some parameter will only be provided by the creating class, then realize it can be provided by a request-scoped injector. If we mix the newable and injectable parameters in the same list, we'll need named parameters so you can say which of your arguments should be assigned to which parameters:

```
class BankServiceImpl(String name, DbConnection c) {
  // some code
}

class Foo() {

  Void myMethod() {
    BankServiceImpl.new(name -> "myName"); // let the injector fill in the DbConnection
  }
}
```

# Even newables are mockable #

We want to guarantee that there is always a seam between two classes, so that they can be tested in isolation. This should even be true for newable types.

```
// in Foo.noop
class Foo() {
  Void method() {
    email = Email.new("alexeagle@google.com", "How's it going?", "Just checking in, thanks");
    email.addAttachment(attachment); // there's some nasty MIME encoding under here, let's say
  }
}
// in FooTest.noop
test {
  Email -> MockEmail;
  Foo.new().method(); // doesn't do the real attachment logic
}
```

So, this means that every object instantiation is handled by the injector. But, in the email class, the injector needs to just accept the user's parameters.
```
class Email(String to, String subject, String body) {
}
```

In the compiler, we will probably want to optimize these calls to be an actual new operation.

# Determining whether a type is newable or injectable #

In this case, we could bootstrap our knowledge by noting that surely String is newable. Since Email takes only newable parameters, Email is newable as well.

Types we know to be newable: String, Int, Array, Date, Float, Map, Enum, etc

Another option is to look at the parameter list, in the square/round paran syntax. A class with only square brackets is injectable. If there are round parens, it is newable.

## Comments? ##
Here is a thread with some discussion about this topic:
http://groups.google.com/group/noop/browse_thread/thread/f03fa72c9545e5c5