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

import org.slf4j.LoggerFactory
import scala.collection.mutable.ArrayBuffer
import types.{NoopString, Injector, NoopBoolean, NoopObject, NoopType};

import interpreter.testing.TestFailedException;
import model.{AssignmentExpression, Block, BooleanLiteralExpression, DereferenceExpression,
    EvaluatedExpression, IdentifierDeclarationExpression, IdentifierExpression,
    IntLiteralExpression, Method, MethodInvocationExpression, Modifier,
    OperatorExpression, ReturnExpression, ShouldExpression, StringLiteralExpression,
    Visitor, WhileLoop};


/**
 * @author alexeagle@google.com (Alex Eagle)
 * @author tocman@gmail.com (Jeremie Lenfant-Engelmann)
 */
class InterpreterVisitor(val context: Context, injector: Injector) extends Visitor {
  val logger = LoggerFactory.getLogger(this.getClass());

  def visit(assignmentExpression: AssignmentExpression) = {
    val currentFrame = context.stack.top;
    val identifier = assignmentExpression.lhs.asInstanceOf[IdentifierExpression].identifier;
    val obj = currentFrame.lastEvaluated(1);

    currentFrame.lastEvaluated.clear();
    if (obj == null) {
      throw new RuntimeException("cannot assign Void");
    }
    currentFrame.blockScopes.setValue(identifier, Tuple(null, obj));
  }

  def visit(block: Block) = {
    context.stack.top.lastEvaluated.clear();
  }

  def visit(booleanLiteralExpression: BooleanLiteralExpression) = {
    val noopBooleanClassDef = context.classLoader.findClass("Boolean");

    context.stack.top.lastEvaluated += injector.create(booleanLiteralExpression.value);
  }

  def visit(dereferenceExpression: DereferenceExpression) = {
    val rhs = context.stack.top.lastEvaluated.pop;
    val lhs = context.stack.top.lastEvaluated.pop;
    val property = rhs.asInstanceOf[NoopString].value;
    val result = lhs.asInstanceOf[NoopObject].propertyMap.get(property) match {
      case Some(v) => v;
      case None => throw new RuntimeException("No such property " + property);
    }
    context.stack.top.lastEvaluated += result;
  }

  def visit(evaluatedExpression: EvaluatedExpression) = {
    context.stack.top.lastEvaluated += evaluatedExpression.value;
  }

  def visit(identifierDeclarationExpression: IdentifierDeclarationExpression) = {
    val currentFrame = context.stack.top;

    if (currentFrame.lastEvaluated.isEmpty) {
      throw new RuntimeException("The right handside didn't evaluate to a proper value");
    }
    val obj = currentFrame.lastEvaluated.top;
    logger.trace("identifierExpression of {} found an initial value {}",
        identifierDeclarationExpression.name, obj);
    currentFrame.lastEvaluated.clear();
    currentFrame.addIdentifier(identifierDeclarationExpression.name,
        new Tuple2[NoopType, NoopObject](null, obj));
  }

  def visit(identifierExpression: IdentifierExpression) = {
    val currentFrame = context.stack.top;
    val identifier = identifierExpression.identifier;
    logger.info("Visiting ID expr: {}", identifier);
    if (identifier == "this") {
      currentFrame.lastEvaluated += currentFrame.thisRef;
    } else if (currentFrame.blockScopes.hasIdentifier(identifier)) {
      currentFrame.lastEvaluated += currentFrame.blockScopes.getIdentifier(identifier)._2;
    } else if (currentFrame.thisRef.propertyMap.contains(identifier)) {
      currentFrame.lastEvaluated += currentFrame.thisRef.propertyMap(identifier);
    } else {
      currentFrame.lastEvaluated += injector.create(identifier);
    }
  }

  def visit(intLiteralExpression: IntLiteralExpression) = {
    val noopIntegerClassDef = context.classLoader.findClass("Int");

    context.stack.top.lastEvaluated += injector.create(intLiteralExpression.value);
  }

  var evaluationStackSize = -1;

  def enter(methodInvocationExpression: MethodInvocationExpression) = {
    evaluationStackSize = context.stack.top.lastEvaluated.size;
  }

  def afterArgumentVisit(methodInvocationExpression: MethodInvocationExpression) = {
    if (context.stack.top.lastEvaluated.size > evaluationStackSize) {
      evaluationStackSize = context.stack.top.lastEvaluated.size;
    } else {
      throw new RuntimeException("Argument to method " + methodInvocationExpression.name +
          " evaluated to Void");
    }
  }

  def visit(methodInvocationExpression: MethodInvocationExpression) = {
    val methodInvocationEvaluator = new MethodInvocationEvaluator(methodInvocationExpression, this);
    methodInvocationEvaluator.execute(context);
    evaluationStackSize = -1;
  }

  def visit(method: Method) = {
    if (method.modifiers.contains(Modifier.native)) {
      val obj = context.stack.top.thisRef;
      val arguments = new ArrayBuffer[NoopObject];
      for (parameter <- method.parameters) {
        arguments += context.stack.top.blockScopes.getIdentifier(parameter.name)._2;
      }
      val returnValue = obj.executeNativeMethod(arguments, method.name);
      context.stack.top.lastEvaluated += returnValue;
    } else {
      method.block.accept(this);
    }
  }

  def visit(operatorExpression: OperatorExpression) = {
  }

  def visit(returnExpression: ReturnExpression) = {
  }

  def visit(shouldExpression: ShouldExpression) = {
    val lastEvaluated = context.stack.top.lastEvaluated;
    val actual = lastEvaluated(0);
    val expected = lastEvaluated(1);

    if (actual != expected) {
      throw new TestFailedException("expected " + actual + " to equal " + expected);
    }
    context.stack.top.lastEvaluated.clear();
  }

  def visit(stringLiteralExpression: StringLiteralExpression) = {
    val noopStringClassDef = context.classLoader.findClass("String");

    context.stack.top.lastEvaluated += injector.create(stringLiteralExpression.value);
  }

  def visit(whileLoop: WhileLoop) = {
    if (context.stack.top.lastEvaluated(0).asInstanceOf[NoopBoolean].value) {
      context.stack.top.blockScopes.inScope("while loop") {
        whileLoop.body.accept(this);
      }
      whileLoop.accept(this);
    }
    context.stack.top.lastEvaluated.clear();
  }
}
