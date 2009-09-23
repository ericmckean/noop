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

package noop.model

import interpreter.Context
import types.NoopObject

/**
 * @author alexeagle@google.com (Alex Eagle)
 */

class AssignmentExpression(val lhs: Expression, val rhs: Expression) extends Expression {

  def evaluate(c: Context): Option[NoopObject] = {
    if (!lhs.isInstanceOf[IdentifierExpression]) {
      throw new RuntimeException("Oops, I only know how to assign to identifiers");
    }
    val identifier = lhs.asInstanceOf[IdentifierExpression].identifier;

    val newValue = rhs.evaluate(c);
    newValue match {
      case Some(newObj) => {
        val currentFrame = c.stack.top;
        if (currentFrame.identifiers.contains(identifier)) {
          currentFrame.identifiers(identifier) = Tuple(null, newObj);
        } else {
          throw new IllegalStateException("No identifier " + identifier);
        }
      }
      case None => throw new RuntimeException("cannot assign Void");
    }

    return newValue;
  }
}
