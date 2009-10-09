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

import collection.mutable.{Map, Stack};

import model.Method;
import types.{NoopType, NoopObject};

 /**
  * @author alexeagle@google.com (Alex Eagle)
  * @author tocman@gmail.com (Jeremie Lenfant-Engelmann)
  */
class Frame(val thisRef: NoopObject, val method: Method) {

  val identifiers = Map.empty[String, Tuple2[NoopType, NoopObject]];
  val lastEvaluated: Stack[NoopObject] = new Stack[NoopObject]();

  def addIdentifier(name: String, arg: Tuple2[NoopType, NoopObject]) = {
    if (identifiers.contains(name)) {
      throw new RuntimeException("Identifier " + name + " shadowing existing identifier.");
    }
    identifiers += Pair(name, arg);
  }
}