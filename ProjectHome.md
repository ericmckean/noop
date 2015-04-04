<h1>Noop</h1>
# What is Noop? #

Noop (pronounced noh-awp, like the machine instruction) is a new language experiment that attempts to blend the best lessons of languages old and new, while syntactically encouraging what we believe to be good coding practices and discouraging the worst offenses.  Noop is initially targeted to run on the Java Virtual Machine.

# What is the status of Noop? #

Right now, we are in an early design and development phase. You can't code anything interesting in Noop yet.

# Why another language? #

Our experience has been that developers often create code that's hard to test and maintain, without realizing it. On a large software project, this can create problems later on for the whole team. In analyzing this problem, we found that the root cause in many cases was language features - like globally visible state, misused subclassing, obligatory and redundant boilerplate, and API's that are easily misused. Noop seeks to apply the wealth of lessons of language development over the past 20 years and optimize on cleanliness, testability, ease-of-modification, and readability.  Rather than innovating on language features, Noop attempts to innovate on the developer's experience.

# Who's behind Noop? #

Noop is a side-project from a collection of like-minded developers and contributers.  We hail from several companies, including (but not limited to) Google.

# Noop is opinionated #

The developers of Noop feel pretty strongly about good and bad practices in software development.  Noop will support or deter these as best as possible.  For example:

<table>
<blockquote><tr><th><b>Noop says <i>Yes</i> to</b></th><th> <b>Noop says <i>No</i> to</b></th></tr>
<tr valign='top'><td>
</blockquote><ul><li>Dependency injection built into the language<br>
</li><li>Testability - a seam between every pair of classes<br>
</li><li>Immutability<br>
</li><li>Syntax geared entirely towards readable code<br>
</li><li>Executable documentation that's never out-of-date<br>
</li><li>Properties, strong typing, and sensible modern standard library<br>
</li></ul><blockquote></td><td>
</blockquote><ul><li>Any statics whatsoever<br>
</li><li>Implementation inheritance (subclassing)<br>
</li><li>Primitive types (everything's an object)<br>
</li><li>Unnecessary boilerplate code<br>
</li></ul><blockquote></td></tr>
</table></blockquote>

## Why Noop? ##

Dependency Injection changed the way we write software. Spring overtook EJB's in thoughtful enterprises, and Guice and PicoContainer are an important part of many well-written applications today.

Automated testing, especially Unit Testing, is also a crucial part of building reliable software that you can feel confident about supporting and changing over its lifetime. Any decent software shop should be writing some tests, the best ones are test-driven and have good code coverage.

Noop is a new language that will run on the Java Virtual Machine, and in source form will look similar to Java. The goal is to build dependency injection and testability into the language from the beginning, rather than rely on third-party libraries as other languages do.

Immutability and minimal variable scope are encouraged by making final/const behavior the default and providing easy access to a functional style. Testability is encouraged by providing Dependency Injection at the language level and a compact constructor injection syntax.

There are three proposed ways to use your Noop source files:

  * Java translator: produces Java source. Allows you to use Noop without converting your codebase, but not all runtime features of the language are provided.
  * Interpreter: reads and evaluates the Noop code through an interpreter. Slower, but will have a command-line interface
  * Compiled: to Java bytecode.

Initial efforts will be focused on an interpreter and Java translator, to allow early experimentation with Noop.  We want people building applications, not ruminating about language concepts.

Get started:
  * Join the mailing list: [noop@googlegroups.com](http://groups.google.com/group/noop)
  * Read about the [Proposed features](Features.md)
  * [Hack](ForDevelopers.md)
  * Read the detailed [Proposals](http://code.google.com/p/noop/w/list?q=label:Proposal)