# Introduction #

Commonly in testing, we have code-under-test which depends on an interface. To supply the dependency, we find that only a small subset of the methods in the interface are really needed. We would like to delegate the rest of the methods to some simple implementation of the interface which just does a noop or returns null for each method.

# Details #

With this interface:
```
interface BankService {
  Int balance();
  // another 100 methods here...
}
```

Just as we can ask the injector for a bound instance of a type with
```
BankService b = BankService.new();
```
we can ask for a default implementation which does noop:
```
BankService b = BankService.noop(); // maybe without parens?
b.balance(); // return null
```

A new class is created at runtime, which is a subtype of BankService.

Likewise, if we want to create a fake which implements some of the methods, but leave the rest as noop, we can delegate:
```
class FakeBankService(delegate BankService b) {
  override Int balance() { return 0; }
}
BankService mock = new FakeBankService(BankService.noop());
```

This would probably work for concrete types as well, by creating a dynamic class which overrides all the methods in the concrete class with noop's. Not sure that's a good idea though.