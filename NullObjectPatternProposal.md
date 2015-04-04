NOTE: THIS PAGE IS DEPRECATED
We've absorbed the comments and some other thinking, and have an alternative proposal in the works.

author: rdionne, christianedwardgruber

# Introduction #

In an effort to tackle the newable vs. injectable problem, we came up with the following example that led to including the Null object pattern directly in the language.  I believe Scala allows similar practice by creating singleton objects that directly inherit from an abstract class.

## Notes ##

The newable vs. injectable issue is not handled in this proposal.  Some noop-specific variations from Java that are under discussion are in play in these examples, such as readonly, writeonly, optional, etc., keywords.  Please treat these as pseudocode in effect, since these have not yet been finalized.

# References #

We are assuming that newable objects are mainly data or value objects (or entity objects) that may have additional convenience calculations attached to them.  These convenience calculations may be dependent upon the presence of optional internal delegates.  There are many situations where an object reference may be null.  If, for example, the object uses an optional delegate, how does a value object perform calculations when an optional delegate objects are not present?  Usually developers add conditional code to check for null and perform alternate calculations when optional items are missing.  We propose supporting two key language features:

1. An objective-c approach to null references
2. Null Object Pattern within NººP.

By doing so, much  null-related conditional logic can be replaced with polymorphism.

## Invocation against null-references ##

When a variable reference is assigned to the null value, we allow calling methods upon the reference, similar to behaviour found in Objective-C.  The default behaviour of a method with no return type is a no-op.  The default behaviour of a method with a return value is to return null:

```
Foo foo = null;
foo.doFoo();  // Does not throw NullPointerException, performs a no-op by default
assertTrue(null == foo.getFoo()); // Does not throw NPE, performs a no-op and returns null by default
```

Chaining of methods results in a chain of this no-op behaviour.

```
BoogaResult result = foo.doBar().processBlah().boogabooga();
```

This would not throw an exception, but would, if any of the above returned null (or if foo was null) would result in null being assigned to `result`.

## Null Object Pattern ##

We allow the developer to override the behavior for a null reference based on its type.  For example, given a type Foo:

```
interface FooInput {
  void increment();
}

interface FooResult { ... }

interface Foo {
  void doFoo(FooInput input);
  FooResult getFoo();
}

null Foo {
  void doFoo(FooInput input) {
    input.increment();
  }

  FooResult getFoo() {
    return new FooResult(5);
  }
}
```

Perhaps such defaults could be provided at a Module level, through injection bindings:

```
null Foo(FooResult default) {
  void doFoo(FooInput input) {
    input.increment();
  }
  FooResult getFoo() {
    return default;
  }
}
```

In the following example, a ShoppingCart object groups LineItems representing products on a purchase order.  Each line item points to its product, a Purchaseable, its base price, a Money instance, and an optional Discount, which calculates the final price of the product after the applied discount.  Rather than include conditional code within LineItem that worries about what to do when a creator creates a LineItem without specifying a Discount, we provide a smart null implementation of Discount:

```
interface ShoppingCart {
  void addLineItem(LineItem item);
}

interface Purchasable { ... }

interface Currency { ... }

interface Money {
  Currency getCurrency();
  long getAmount();
}

interface Date { ... }

interface Discount {
  Money apply(Money basePrice, Date date);
}

null Discount {
  public Money apply(Money basePrice, Date date) {
    return basePrice;
  }  
}

createable LineItem {
  readable Purchasable product;
  readable Money basePrice;
  optional readable writeable Discount discount;
  readable virtual Money finalPrice {
    get {
      return discount.apply(basePrice, Date.now());
    }
  }
}
```
Usage:
```
ShoppingCart shoppingCart = ...;

Purchasable catamaran = ...;
Money catamaranBasePrice = ...;
Discount catamaranDiscount = ...;
LineItem catamaranPurchase = new LineItem(catamaran, catamaranBasePrice, catamaranDiscount);

assertTrue(catamaranPurchase.finalPrice < catamaranPurchase.basePrice);

Purchasable carousel = ...;
Money carouselBasePrice = ...;
LineItem carouselPurchase = new LineItem(carousel, carouselBasePrice); // No discounts here

assertTrue(carouselPurchase.finalPrice == carouselPurchase.basePrice);

shoppingCart.addLineItem(catamaranPurchase);
shoppingCart.addLineItem(carouselPurchase);

Money grandTotal = shoppingCart.total();
```