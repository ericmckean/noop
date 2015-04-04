## Abstract ##

As a core language feature of Noop, we propose an approach to handling the risk of de-referencing null references based closely on the draft of a paper on avoiding null (void) dereferences in Eiffel<sup><a href='#References.md'>(1)</a></sup>.  The basic approach is to assert a static assurance at compile time that a given reference which will undergo a de-referencing operation can't be null, by means of "non-null types" which are guaranteed to have not null values at all times, static proof of null-safety of statements at compile-time, and the application of language patterns which will certify the null-safety within a block where types and references are nullable.  Additionally, to eliminate boilerplate, null reference checks, we propose to add an additional notion of "null tolerance" with a convenient (and explicit) syntax to allow expressions where the program author  prefers a well defined behaviour of short-circuiting the statement and performing a no-op or returning null, as appropriate, similar in semantic to null dereferences in Objective-C.

The style and syntax of these combined solutions proposed in the paper will be adapted to the context of Noop, which has the advantage of starting from scratch, rather than attempting to create "null safety" in an existing language (Eiffel), as did Meyer et. al. This combination, we hope, will not only provide an elimination of null dereference exceptions from any running Noop code, while providing the convenience of eliminating boilerplate null checks in places where further computation is rendered irrelevant by an earlier null reference.

## Proposal ##

### Preamble ###

#### Principles and Constraints of Design ####

All Noop language features need to balance several key principles.  This proposal attempts to keep the following principles in mind:

Code written in Noop should always:
  1. be modular, detachable (many seams), and testable
  1. have a minimum of boilerplate required to code the solution
  1. be reasonably terse
  1. be easily readable by unfamiliar developers
  1. encourage desired patterns and discourage undesired patterns
  1. empower the developer - limitations should come with preferred alternatives

In light of these, Noop's solution to handling nulls and calling functions across references that may be null needs to be semantically clear, avoid the potential for a possible null reference error and where possible, not burden the developer with unnecessary boilerplate.

#### Context ####

As pointed out in the paper mentioned above, Nulls have important semantic meaning, and even with the Null-Object Pattern (reference) the need for a value for non-existance is necessary for such things as terminating data structures, and may be necessary to avoid subtle error where a null object may have behaviour suited to one context, but in another context it would be preferable to crash than to return wrong data.  Membership in a list is another example - a null object in a list can bloat the contents of a list, and cause incorrect size data to be returned, when the true semantic is non-membership in, or absence from, the list.  It may be desirable to eliminate null, but it simply shifts the kinds of error, and doesn't save the developer anything over an approach of clear null safety.

#### Credit ####

Large parts of this proposal follow the structure and content of the aforementioned paper

### Null Safety ###

By Null-Safety, this proposal means that a given operation which involves the traversal of a reference is guaranteed to be traversing a non-null reference.  The property of null-safety

#### Null-Safe references ####

#### Transitivity of Null-Safety ####

#### Null-Safe Certification Blocks ####

##### Local References #####
```

X x = getX(); // where getX() is a function that may return null.
if x exists {
  x.doSomething(); // guaranteed to be a valid dereference.
}

```

##### Object properties #####

Nullable object properties, unlike local references, are more ambiguous, because they can be affected by multiple threads of execution.  In such circumstances, a race-condition could apply whereby the reference is tested, but before it is used, it is replaced by a null reference.

The syntax of exists is easily adaptable by allocating a local variable atomically at the time of the test, which is then used as the reference within the block, to ensure that the reference is the proven one.

```

public class Foo {

  public nullable Bar bar;

  public void processBar() {
    if bar exists as _bar {
      _bar.process(); // this is null safe
      bar.process(); // this throws a compiler error
    } else {
      // perform some default behaviour.
    }
  }

}

```

Any local variable name can be used, but by practice, _variable is appropriate, to keep from distorting the code too much.  This code is more or less the equivalent of doing the following in Java:_

```

public class Foo {

  private Bar bar;

  public Bar getBar() { return bar; }

  public void setBar(Bar bar) { this.bar = bar; }

  public void processBar() {
    Bar _bar = bar;
    if (_bar != null) {
      _bar.process(); // this is null safe
      bar.process(); // this throws a compiler error
    } else {
      // perform some default behaviour.
    }
  }

}


```

### Null Tolerance ###

Options:
- OLGN-style ?.
- Alternate ?> - less easy to miss
syntactic sugar - wraps a test and fast fail - entirely consistent with Null Safety

explicit semantic - developer has to choose

only applicable to nullable references. compiler error to use otherwise.



### Conclusion ###


## References ##

  1. ["Avoiding a Void: The eradication of null dereferencing"](http://docs.eiffel.com/sites/default/files/void-safe-eiffel.pdf) by Meyer, Kogtenkov, and Stapf (2009)