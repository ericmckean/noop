# Introduction #

Checked exceptions cause a lot of problems, and don't buy much, so Noop only has unchecked exceptions.

Coders often use exceptions for expected error states. An example:

```
// in Java
FileOutputStream out;
try {
  out = new FileOutputStream(new File("/tmp/foo"));
} catch (FileNotFoundException e) {
  // handle it
}
out.readline();
```

Using exceptions to check for "expected" or "likely" error conditions is usually not an appropriate use of exceptions. Creating the exception requires capturing a stack trace, and the try-catch block interrupts the control flow and creates the ugly scope, in order to provide a reference to the exception. In this example, it requires declaration of the variable out in a null state so it must be mutable.

This Java code could check for the file existence before trying to read from the file.
```
File file = new File("/tmp/foo");
if (file.exists()) {
  FileOutputStream out = new FileOutputStream(file);
  out.readline();
} else {
  // handle it
}
```

But, this requires the File API to do some work twice. Some API's are especially bad for this sort of work, like having to run two database calls instead of one just to support this conditional.

This proposal allows a way to hand an error back from a single API call in a more declarative way than with an unchecked exception, without the scoping issues and stack trace creation in an exception.

# Details #

In the spirit of C's error out variables, some methods may declare that they return an optional error in addition to the returned value. This allows the caller to handle the error in a normal control flow.

Here is an example with another common case, parsing a number from a String. A failure to parse is an expected error.
```
class Int() {
  Int, Error parse(String s) {...}
}

Int a, Error e = new Int().parse("13a");
if (e.isError()) {
  // handle error
} else {
  // a must be a good int
}
```

We can return some sentinel value for a in this case, like zero. This has some advantages if the caller doesn't care to check this for an error, ie. there is some other validation of the value.

The caller should be able to ignore the error, also.
```
Int a, _ = new Int().parse("100");
```