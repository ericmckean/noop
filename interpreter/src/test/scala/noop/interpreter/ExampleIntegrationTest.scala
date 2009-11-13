/**
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package noop.interpreter;

import com.google.inject.Guice
import java.io.{File, ByteArrayOutputStream}
import types.NoopTypesModule;

import org.scalatest.matchers.ShouldMatchers;
import org.scalatest.Spec;

import grammar.Parser;

/**
 * This test runs all the example noop programs found under /examples.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
class ExampleIntegrationTest extends Spec with ShouldMatchers {

  def createInjector() = {
    Guice.createInjector(new InterpreterModule(List()), new NoopTypesModule());
  }

  def createFixture = {
    val sourcePaths = List(
        new File(getClass().getResource("/helloworld").toURI).getAbsolutePath(),
        new File(getClass().getResource("/controlFlow").toURI).getAbsolutePath(),
        new File(getClass().getResource("/arithmetic").toURI).getAbsolutePath());
    new SourceFileClassLoader(new Parser(), sourcePaths);
  }

  def withRedirectedStandardOut(testFunction: ByteArrayOutputStream => Unit) {
    val originalOut = Console.out;
    val output = new ByteArrayOutputStream();
    try {
      Console.setOut(output);
      testFunction(output);
    } finally {
      Console.setOut(originalOut);
    }
  }

  it("should run the hello world program") {
    withRedirectedStandardOut { (output) => {
      val classLoader = createFixture;
      val mainClass = classLoader.findClass("HelloWorld");
      mainClass should not be(null);

      createInjector().getInstance(classOf[Interpreter]).runApplication(mainClass);
      output.toString() should include("Hello World!");
    }}
  }

  it("should run while loop") {
    withRedirectedStandardOut { (output) => {
      val classLoader = createFixture;
      val mainClass = classLoader.findClass("WhileLoop");
      createInjector().getInstance(classOf[Interpreter]).runApplication(mainClass);
      output.toString() should equal("Hello World!\n");
    }}
  }

  it("should run the arithmetic program") {
    withRedirectedStandardOut { (output) => {
      val classLoader = createFixture;
      val mainClass = classLoader.findClass("Arithmetic");
      createInjector().getInstance(classOf[Interpreter]).runApplication(mainClass);
      output.toString() should include("3");
    }}
  }
}
