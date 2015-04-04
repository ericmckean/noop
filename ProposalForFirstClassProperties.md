<a href='Hidden comment: 
Author: Christian Edward Gruber (christianedwardgruber@gmail.com)
'></a>

**-DRAFT-**

# First Class Properties in NººP #

## Introduction ##

Boilerplate code is evil.  Java, sadly, in order to follow recommended best practices around isolating access to the internals of an object have adopted the "bean" approach, with convention-driven accessors and mutators.  Adding first-class language support for lazy declaration of mutators and accessors would allow the proper encapsulation to occur without requiring "just in case" boilerplate code, with the result that calling code need have no knowledge of whether it is obtaining or setting a property value via acessors/mutators or directly from the field.

## Ancestry of the approach ##

Accessors and mutators should be declared specifically within the language as a first class language element.  I propose a similar approach to C# in this regard.  In fact, the property syntax (roughly) of C# (with minor modifications) would be the only means by which to create an instance variable

## Priorities ##

This proposal has a few key priorities that motivate it.  This include, but aren't limited to:
**Eliminating boilerplate code** Buffering client (calling) code from changes to storage, strategy, or implementation details of the property
**Enforcing good encapsulation**

## Basics - declaration, storage, and use ##

Properties will be declared similarly to instance variable in traditional languages, but setters and getters will be declared similarly to C# properties. The most basic example is:

```
public class Foo {
    public Bar bar;
}
```

The semantics of this declaration are that a public property named foo is declared with type Foo, and that no special access or mutation logic is present.  Such a property is accessed the way variables of the same scope (public in the above case) are traditionally accessed in Java or C#.  i.e.:

```
     Foo foo = new Foo();
     foo.bar = new Bar();  // (sets foo's "bar" property)
     Bar bar = foo.bar; // (gets foo's "bar" property)
```

The implications of this are that there is a set and get logic which mediates access to the raw value.  But where is the raw value?

Storage of a property is, in many languages, separate from the declaration of accessors.  Java certainly does this, but C#, even with language support for properties also does so.  The property approach in NººP should consider a property as a more atomic language feature - in fact, it is the only way to declare an instance variable.  All fields are at least implicitly properties (though declaration of mutation and accessor logic other than the default should be optional).  This implies that storage also shouldn't be separate, but implied in the property declaration, unless explicitly disclaimed. The underlying storage can be accessed directly only within the class itself, by means of a dereferencing symbol @.    In the case of Foo above, one would access the storage with @bar.  However,  @bar would have no meaning outside of the class itself.  One would not be able to invoke foo.@bar to circumvent the property's get/set logic.  Being private to the instance, the raw storage created this way would also not be accessible by any children or other entities.

A declaration like this:

```
    public Foo foo {
        set {
            @foo = foo;
        }
        get {
            return @foo;
        }
    }
```

... would be like the above, but override the default access/mutation logic.  This example, simply re-implements the default access/mutation strategy.

A property with no storage could be created as in the following example, which delegates to a service which was injected.

```
public class(SomeService service) { 
    public virtual Foo foo {
        set {
            @service.processFoo(foo);
        }
        get {
            return @service.fetchFoo();
        }
    }
}
```

Because the above is virtual, no storage is created for **foo** and so the following code would break.

```
public class(SomeService service) { 
    
    public virtual Foo foo {
        set { @service.processFoo(foo); }
        get { return @service.fetchFoo(); }
    }

    public Void doSomethingOnFoo() {
        // this line is valid, as it accesses via the property
        foo.doSomething(); 
        // no such raw value for foo exists, so this breaks compilation
        @foo.doSomething();
    }
}
```

## Buffering client code change in the implementation ##

Consider an initial implementation of a class:
```
public class Foo {
    public Bar bar;
}
```

Calling code would be able to access this thusly:

```
   Bar bar1 = new Bar();
   Bar bar2 = new Bar();
   Foo foo = new Foo();
   foo.bar = bar1;
   assertEquals(bar1,foo.bar);
   myFoo.bar = bar2;
   assertEquals(bar2,foo.bar);
```

If custom logic was added to modify the access approach - say, a wrapper that does something magical, then Foo might be changed thusly:

```
public class Foo(Store store, User user) {
    public Bar bar {
        get { return store.fetchBarForUserWithId(user.id); }
        set { store.saveBarForUserWithId(bar, user.id); }
    }
}
```

(Yes, the above example of storage could be written differently with alternate injection approaches, but this example isn't intended to illustrat that here)

As long as the wrapper kept the equality semantic the same, the client code would not know the difference.  The client code doesn't have any knowledge about the internals.