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

package noop.model;

import interpreter.Context;
import types.NoopObject;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
class IdentifierExpression(val identifier: String) extends Expression {

  def evaluate(c: Context): Option[NoopObject] = {
    val currentFrame = c.stack.top;
    if (identifier == "this") {
      return Some(currentFrame.thisRef);
    } else if (currentFrame.identifiers.contains(identifier)) {
      return Some(currentFrame.identifiers(identifier)._2);
    } else if (currentFrame.thisRef.parameterInstances.contains(identifier)) {
      return Some(currentFrame.thisRef.parameterInstances(identifier));
    }
    return None;
  }

  override def toString() = identifier;

}