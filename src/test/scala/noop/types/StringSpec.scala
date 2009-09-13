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

package noop.types

import grammar.Parser
import interpreter.ClassLoader
import java.io.File
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec

/**
 * @author alexeagle@google.com (Alex Eagle)
 */

class StringSpec extends Spec with ShouldMatchers {
  def createFixture = {
    val stringSourcePath = new File(getClass().getResource("/stdlib").toURI).getAbsolutePath();
    new ClassLoader(new Parser(), List(stringSourcePath))
  }

  describe("a Noop String") {
    it("should have a valid class definition parsed from Noop source") {
      val classLoader = createFixture;
      val classDef = classLoader.findClass("String");
      classDef.name should be("String");
    }

    it("should have a native implementation of the length method") {
      val classLoader = createFixture;

    }
  }
}