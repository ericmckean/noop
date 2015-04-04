# Introduction #

There are many layers needed for tools to work with a traditional programming language:
  * Lexer
  * Parser
  * Abstract syntax tree
  * Abstract semantic graph

The latter is ultimately the form that's understood by a compiler or an IDE. For example, IntelliJ IDEA calls the semantic graph a PSI model which is modified by user actions such as refactoring.

http://www.jetbrains.com/idea/openapi/5.0/com/intellij/psi/package-summary.html
http://www.jetbrains.com/idea/plugins/developing_custom_language_plugins.html

The programmer understands the program by reading and writing text files, which are an encoded representation of the program conforming to a certain fixed grammar which is particular to that language. The tooling must keep that textual representation of the program in sync with the abstract semantic model. When a refactor operation begins, the IDE must have the entire program represented in the semantic model, so that text in any file may be changed as needed.

Because there are many ways to represent the same semantic graph with a textual representation, we have to deal with style and formatting. A team must pick a common style to apply across their code, for uniformity in reading and to avoid spurious changes appearing in changelogs and code reviews. Ideally, such a style guide restricts the options in syntax such that there is only one canonical textual form for a certain semantic graph, but this ideal is not feasible. What's more, a great deal of needless busy work is required from developers to adhere to the style, even when the IDE supports the style with a formatting tool, because of arcane differences like the alignment of a continued line in a list of parameters.

The textual representation also restricts what is possible in a code viewer. Every reader must see the code with the same syntax, and every editor must be largely based on a text area in which the act of coding takes place. At one time, it was the fashion to use the TAB character to indent lines of code, so that a reader could choose their own comfortable setting based on their screen width and fonts. But this lead to problems with a mix of tabs and spaces, so that many teams disallow the tab character altogether. And the size of the indent was only a small amount of control to grant the reader of code.

A code review or diff tool is even more encumbered. While the concept behind a change may be best explained as a refactoring that took place, or a simple rename of a method, the resulting changes to the textual representation of a program include many which do not relate directly to that concept. Instead, there are syntactical changes such as line breaks introduced to honor the maximum column width allowed, references to the method which change when the method name changes, import statements which change when a class is moved to a different namespace. Some engineers are quite adept at looking through a diff and mentally working backwards to the sequence of semantic model events which created them. But it's a lot to ask, and makes code reviews more challenging and less effective.

This way of working with code is quite baked into our ideas of programming, and is also the defacto way in which all development tools integrate. Version control systems, code review tools, and messages from a compiler all work in terms of files and lines, and thus require no knowledge of each other. This is very convenient indeed.

**This proposal suggests that we shift this central integration point from text files on disk to a semantic model in memory.** This does not preclude a text-based editing environment, it merely shifts the responsibility of dealing with the lexing, parsing, and abstract syntax tree binding to that editing environment. Other language tools, including semantic editors, may work with the code directly in the semantic form. All of the concerns listed above are alleviated by this conceptual shift.

One objection to this approach is that tooling becomes a requirement of reading or editing code. It is no longer possible to download a file to disk and open it in vim or emacs. Instead, some intermediate program must dump the semantic model out to files, using a mapping to an AST and a formatter based on a syntax. This is not a great cost to bear, but more importantly, even the vim or emacs user has a set of tools which they use to perform refactorings, find incoming references to a language element, and so on. They may use a syntax highlighter to help their brain parse the code more quickly. They have gained a mastery of grep, sed, and other unix tools, or they rely on a compiler or tests to display a listing of broken pieces. So tooling is always a requirement for reading or editing code. The real objection here is that these developers want to work in terms of the raw code, not manipulate the code through a tool which pretends that the code has a semantic layer over top. IDE's are slow and clumsy ways of making a certain fixed modification to a file, when you know exactly what the correct modification is. When the semantic model is the raw code, this objection falls away.

Another objection is the scope of the changes which will be needed. Most existing tools operate only on text files. The API for working with code is at present not really an API at all, just a convention that the file's contents should be modified with care, and saved back to disk. Thinking in these terms, we just need a new API that reflects the act of reading and editing code, and allow tools to begin speaking that API. If it is a good one, I theorize that tool authors will enjoy how easily they can work with code that's represented semantically, and will write good tools. Since it takes five years to develop a language, there is time.

# The semantic model #

The semantic graph of a program is modeled, like any graph, as a sequence of vertices and their edges. Each vertex represents an element of the language model. such as a Class or Method, and each edge is a relationship between elements. The semantic graph is really a set of directed graphs of several types:
  * Contain is the parent-child relationship, eg. a Class contains a method. The Contain graph is a tree, which contains every element.
  * Invoke is the relationship from a callsite of a method or a function to the definition of that method or function.
  * Target relates a callsite of a method call to the instance of the object which should be provided to the method dispatch.
  * Type is a relationship from an element to the definition of its Type.
  * Override is a relationship between two methods, in which they share a signature but in a certain scope one is used instead of the other.
  * There are probably more...

Vertex types:
  * Workspace which is a root of the editing environment, and not persisted.
  * Project
  * Library
  * Class
  * Block can represent a method (if it is named and takes a this reference), a function (named with no this) or a plain block
  * MethodInvocation
  * Expression
  * StringLiteral
  * etc.

The workspace holds the state of the semantic graph at any time. Persisting the workspace to disk allows the editing environment to terminate and later restart, by de-serializing the workspace back into an object graph.

The graph is represented as an Array

&lt;Vertex&gt;

 and a Array<List

&lt;Integer&gt;

> for each relationship type. Each vertex has a natural integer identifier, which is its position in the vertex array. The edges are sparsely populated, so they are represented as a list of  destination vertices for each source vertex.

Each Vertex is an immutable object. Rather than being changed, it holds a pointer to the previous version of the vertex.

The fundamental unit of modification is either an add vertex or edit vertex operation. The add operation appends a given new vertex to the end of the vertex array, along with a contain edge from its parent, and any new edges from the new vertex to other existing vertices. The edit operation creates a new copy of the vertex with the modifications, pointing to the previous version, and replaces the entry in the vertex array.