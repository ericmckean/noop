# Introduction #

Documentation styles and conventions have rules that could be enforced by a syntax. Documentation builtins for the language allow developers to build tools by accessing the documentation API's from a standard library. For example,  looking in a class description for info about a specific parameter, at what version a class was added to the API, etc. The runtime could also inspect documentation to generate more helpful or readable error messages or suggestions.

Python has a concept of docstrings, though we can take this a step further and enforce a documentation syntax that can also be parsed by a compiler.

If the documentation is code then you wouldn't need to ship or provide separate documentation with your build. The closer the documentation lives to the code the more likely it is to be correct.

Our aim is to make writing good documentation easy, and help keep it up-to-date.

# Details #

How documentation will be written in Noop:
  * Output-agnostic: no HTML markup in the documentation!
  * A `doc` keyword appears as part of a property, method, or class declaration.
  * The doc keyword is followed by a single Noop string literal
  * The content of the doc string will need escaping to avoid terminating the string early. A multi-line string literal will make this rare.
  * The content of the doc string will be rendered using a tool like Markdown and/or Textile, Almost Plain Text, or AsciiDoc
  * Use existing javadocs or doxygen metadata such as @param, @return, @author, @todo, @see (http://www.stack.nl/~dimitri/doxygen/commands.html)
  * Support for example code blocks
  * Support for having example code verified
  * Doc "commands" should validate.. like @since major.minor.tiny or @author Name<email@address.tld>
  * Usual wiki rules apply, including auto-linking CamelWords

```
doc """
    A brief description which ends at the first dot followed by a space or new line. 
    Details follow here.
    @param bar Bar description
    @since 1.1
"""
class Foo(Bar bar) {

  doc """
      A brief description which ends at the first dot followed by a space or new line. 
      Details follow here.
      @param bar Bar description
      @return Foo for bar 

      @code
      Foo foo = obj.foo(bar1, bar2);
      @endcode
   """
  public Foo foo(Bar bar, Bar bar) {
  }
}
```

# Warnings #
We check
As suggested by Robert.Jay.Gould, we should warn if a "non-trivial" method or class is missing documentation.

If we have documentation in some known wiki syntax, we may get enough hints for the parser to understand the boundaries without too much comment-looking syntax. ie. if a header is '== Level 2 Header ==', then maybe == is the start of a documentation block?

# Mixing documentation with retained metadata #
Also, we want to have retained metadata (like Java's annotations with @Retention(RUNTIME)) - if we are parsing the documentation, then the metadata may as well be defined along with things like @author. Then we just need to figure out what to retain at runtime and what makes our footprint (class sizes and runtime permgen) too big. This leads to the funny possibility of having the @author available at runtime and blaming them in a stack trace...

# Patching documentation from multiple sources #
Contributed by Arnold.Barber:
One of the problems with Java development documentation is the separate islands of documentation we have to deal with. You start a project and you have the language documentation. You add a library or tool and they each have their own separate documentation. Soon you end up with 20 independent islands of documentation. What I would like to have solved is the way in which multiple NoopDocs?? (Documentation generated from Noop source code) can be merged together. In this way, the underlying Noop classes can show the subclasses, even in your application system. The documentation from your own employer's source code can be directly linked down through the layers of external libraries and tools to the underlying Noop documentation. I know this is technically possible because I have written a tool that post-processes multiple trees of generated JavaDoc?? to produce a single integrated JavaDoc??, increasing the hyperlinks across the lot. This would be something worth getting Noop for.

# TODO(robertsdionne): Executable preconditions/invariants/postconditions #

# Alternatives considered #
  * Putting documentation after the element being documented. We don't like this as much because it's potentially far from the signature of the element.
  * Use an existing comment style like slash-star. We don't like it as much because we want to differentiate documentation from comments
  * 