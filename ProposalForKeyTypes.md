# Introduction #

Guice can bind several different Strings, for example, which is commonly used with command-line flags. This is also useful when binding a constant value.

In Guice, you can either create your own annotation for the Name of the string, or you can use @Named("") to create the name.

Guice then binds using a Key, which is a (Type, Name) pair, ie
```
bind(Key.get(Service.class, Transactional.class)).to(TransactionalService.class)
bindConstant().annotatedWith(Port.class).to(9876);
```

# KeyTypes #

Most of the time, a Guice binding annotation is only ever used with one Type. So it's useless to have to refer to the type everywhere. In Noop, we have a type for each (type,name) pair, called a KeyType, which makes injection easier. It's very much like a named subclass of the type, ie. `class Port extends Int ` would give us a Port type to bind to. But we don't have a use for a subclass.

A Noop KeyType is declared anywhere a class may be declared. Since it has no body, it is rather silly to have a whole file for it, so the expected common use is declaring inside a class body. The declaration looks like this:

```
class Flags() {
  alias Int Port;
}
```

Now you can bind an Int to the Port typekey:
```
Port => 9876;
```
And you can inject the port number:
```
import Flags.Port;
class Server(Port portNumber) {}
```

# Alternative #
I've (robertsdionne) got my own take on how this should work.

Guice allows the developer to primarily bind his dependencies using types as keys.  By default, all declared dependencies of type Foo will be bound to a new instance of Foo, created by the injector.  An alternative is to bind Foo within the singleton scope, so all declared dependencies of type Foo will be bound to a single instance of Foo.  Sometimes developers require two or more singleton objects bound to the same type, but fulfilling dependency requirements of different subsets of the object graph.  Guice solves this with annotations, either like @Bar or @Named("Bar").

The source code, however, already provides a named identifier for a dependency -- the variable name.

I propose we forego using @Bar-style or @Named("Bar")-style annotations and just use the variable name.  For example, instead of the following style:
```
// Bindings.module
bind Bar annotated with @Red to singleton instance new Bar(...);
bind Bar annotated with @Blue to singleton instance new SpecialBar(...);

// Foo.noop
class Foo(delegate @Red Bar red, delegate @Blue Bar blue) {
  public void doBarOperationOne() delegateto red;
  public void doBarOperationTwo() delegateto blue;
}
```

We would use this style:
```
// Bindings.module
bind (Bar red) to singleton instance new Bar(...);
bind (Bar blue) to singleton instance new SpecialBar(...);

// Foo.noop
class Foo(delegate Bar red, delegate Bar blue) {
  public void doBarOperationOne() delegateto red;
  public void doBarOperationTwo() delegateto blue;
}
```

Check out my second comment on ProposalForTestingApi for another example.

My proposal has a few problems:
Now the names of our dependencies matter.

Are name collisions possible?
They might be.  However, I would encourage a policy where the Type-Name pair acts as the key for the purposes of injecting dependencies, declaring dependencies within class declarations, and choosing names for dependency declarations.  For example, class Foo above depends upon (Bar red), but we should allow another developer writing code in other areas of our project to perhaps implement a class Cat(Hair red, Hair blue).  Here, red and blue do not collide with the dependency declaration list of class Foo since what matters is the Hair-"red" key and the Hair-"blue" key, not only the variable names.  However, within the context of code definitions, red should uniquely identify such a Bar within the scope of the class Foo, and red should uniquely identify such a Hair within the scope of the class Cat.

What happens if somebody introduces a typo and a declared dependency no longer has an associated binding within the Module?
We will issue a compiler warning that notifies them of an unbound dependency of key $Type-$Name.

Let me know what you think.  Maybe I'm dangerously close to confusing developers by mixing the meaning of variable identifiers and dependency injection keys.

Also, if the developer writing a given class dislikes the name of a dependency given in the Type-Name key, I think we should allow him to rename it:
```
// Bindings.module
bind (Bar red) to singleton instance new Bar(...);
bind (Bar blue) to singleton instance new SpecialBar(...);

// Foo.noop
class Foo(delegate Bar red as myBar, delegate Bar blue as mySecondBar) {

  public void doBarOperationOne() delegateto myBar;
  public void doBarOperationTwo() delegateto mySecondBar;
}
```

I also like this "as" syntax in the context of unit test mock expectations:
```
expect foo.doSomething() and return 500 as myResult;
expect bar.useResult(myResult) and return 1500 as secondResult;

...
assertSomethingAbout(secondResult);
```