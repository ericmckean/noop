# Getting Started #

Install Java, of course. We target Java 6. Set your JAVA\_HOME environment variable. Also make sure this JDK is on your PATH.

You'll also need Scala installed, we are on version 2.7.5. Set SCALA\_HOME to point to your Scala install.

> Bobby adds: If you run Ubuntu, installing Scala via aptitude does not provide the correct directory layout
> and does not define SCALA\_HOME. I had to apt-get uninstall scala scala-library, download Scala from
> the website as a tar archive, and install it manually.

## Get the source ##

First, install Mercurial (http://mercurial.selenic.com/wiki/)

Clone the repository to your local machine (http://code.google.com/p/noop/source/checkout)

## Build Environments ##

Noop is primarily built with Buildr, though there is a parallel Maven build which we might switch to if we hit too many roadblocks adopting Buildr.

### Buildr ###

You can run the build with either C Ruby or JRuby. With JRuby, however,
  * stack traces are a mile long because they contain frames for all the internal ruby execution calls in JRuby. With C Ruby, your stack trace only contains frames from the invocation of the test.
  * There's a bug where your temp directory (where dependencies are downloaded) must be on the same physical volume with your project directory. (The JRuby bug is this one: http://jira.codehaus.org/browse/JRUBY-3381 Buildr has an open bug which depends on the JRuby bug: http://issues.apache.org/jira/browse/BUILDR-292) You can work around it like this: `TMPDIR=/Users/alexeagle/tmp buildr`

Setup Ruby:
  * Not recommended: Install JRuby (http://dist.codehaus.org/jruby/) - 1.3.1 is the current version Noop is tested with. Set your PATH variable to include the jruby bin directory.
  * Recommended: Check that you have C ruby available on your machine: `ruby -v`. Also be sure you have the ruby development library, or you'll be missing header files when you try to compile a gem, and get an error like `extconf.rb:6:in 'require': no such file to load -- mkmf (LoadError)`. On ubuntu, do this: `sudo apt-get install ruby1.8-dev`. On Mac, Header files are not delivered by default with Mac OS X, you need to install the Xcode Tools package after the installation. You can find it in the Optional Installs / Xcode Tools directory on the Leopard DVD.

Install Buildr (http://buildr.apache.org/). We are using version 1.4.0, which is unreleased, but has much improved support for scalatest. Follow instructions here: http://buildr.apache.org/contributing.html#edge
which are basically
  * `svn co http://svn.apache.org/repos/asf/buildr/trunk buildr`
  * `cd buildr; rake setup install`
  * If root doesn't have JAVA\_HOME set, you may get an error that looks a lot like missing Ruby headers.

Now you can run a build, which will compile and run tests: `buildr`

### Maven (optional) ###
**_`NOTE: Maven build is currently broken -2009.09.28`_**

If you want to try the alternate build:

Download a version of maven from http://maven.apache.org/
  * Must be after 2.0.9
  * 3.0-SNAPSHOT works as of 08/23/09 if you want to be experimental

run `sync.sh` to generate a pom.xml from the pom.yml (we're using a YAML version of the pom). This uses the init.settings.xml file to bootstrap, downloading the yamlpom-plugin which does the magic.

Now you can run `mvn clean install`.

## IDE Setup ##

You'll need to pick an IDE... some members use IDEA and some use Eclipse, so we're agnostic. Keep in mind that Scala support varies. As of 9/9/09, the IDEA plugin seems to work best. Also, we have checked in the .ipr file for IDEA, so you can open the project right away.

### IntelliJ IDEA ###
The free community edition works fine for developing on Noop. I use version 90.193 right now, which is the latest EAP (early access program) release of the community edition from JetBrains.

Install the Scala plugin. I had a problem with the current nightly build not working with the latest IDEA, so try version 0.3.108 if you have trouble.
http://download-ln.jetbrains.com/scala/scala-intellij-bin-0.3.108.zip

If you make changes to the buildfile, you can run `buildr idea` to update the IDEA project metadata. Make sure it doesn't make extra unwanted changes, though. I have problems with the scala facet disappearing, and the path to the scala libraries is wrong.

### Eclipse (generated project files) ###

> To generate the eclipse metadata files (.project and .classpath), run `buildr eclipse` or `mvn eclipse:eclipse`. We don't commit these files because they can be stale, and the buildfile is the source of truth.

> You'll also need to set the M2\_REPO classpath variable in Eclipse, point it to your ~/.m2/repository folder.

### Eclipse (using maven integration) ###

> If you're using maven, and you use the m2eclipse plugin, you can simply "import as Maven project" after you generate the pom.xml in the above maven instructions.

# Submitting (as a committer) #

Set up your Mercurial:
  * Set your global username for mercurial
```
cat >> ~/.hgrc

[ui]
username = Firstname Lastname <username@gmail.com>
```
  * Set the default push location in the repository dir
```
cat >> .hg/hgrc

[paths]
default = https://noop.googlecode.com/hg/
default-push = https://username:gcpassword@noop.googlecode.com/hg/
```

There is a continuous build running at http://jakeherringbone.com:8080/job/Noop/

If you commit and break the build, we may roll your change back and ask you to fix on your own machine.

# Submitting a patch (non-committers) #
Email your patch to noop-dev@googlegroups.com. And thank you!

# Modifying the Grammar #

ANTLRWorks is a nice editor for ANTLR grammars. Get it here: http://www.antlr.org/works/index.html

Lexer/Parser/ASTParser will be generated by ANTLR as Java source, but the rest of the classes are in Scala. The build takes care of this. There is a little interesting stuff when the Java code embedded in the ANTLR grammar wants to call setters, getters, and append methods in Scala-compiled classes. Those methods are written to bytecode as `foo()`, `foo_$eq(value)`, and `foolist().$plus$eq(newval)`, respectively.