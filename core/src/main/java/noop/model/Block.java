/*
 * Copyright 2010 Google Inc.
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

import com.google.common.collect.Lists;
import noop.graph.ModelVisitor;

import java.util.List;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public abstract class Block<T> extends LanguageElement<T> {
  public final String name;
  protected final List<Parameter> parameters = Lists.newArrayList();
  protected final List<Expression> statements = Lists.newArrayList();

  public Block(String name) {
    this.name = name;
  }

  @Override
  public boolean adoptChild(LanguageElement child) {
    if (child instanceof Parameter) {
      parameters.add((Parameter) child);
      return true;
    }
    if (child instanceof Expression) {
      statements.add((Expression) child);
      return true;
    }
    return super.adoptChild(child);
  }

  @Override
  public void accept(ModelVisitor v) {
    for (Parameter parameter : parameters) {
      v.enter(parameter);
      parameter.accept(v);
      v.leave(parameter);
    }
    for (Expression statement : statements) {
      v.enter(statement);
      statement.accept(v);
      v.leave(statement);
    }
    for (UnitTest unitTest : unitTests) {
      v.enter(unitTest);
      unitTest.accept(v);
      v.leave(unitTest);
    }
    super.accept(v);
  }
}
