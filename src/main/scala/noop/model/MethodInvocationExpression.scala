package noop.model;

import interpreter.{Context,Frame};
import types.{NoopObject,NoopType};
import scala.collection.mutable.Buffer;

/**
 * @author alexeagle@google.com (Alex Eagle)
 * @author tocman@gmail.com (Jeremie Lenfant-Engelmann)
 */
class MethodInvocationExpression(val name: String, 
    val arguments: Buffer[Expression]) extends Expression {

  def evaluate(context: Context): Option[NoopObject] = {
    val stack = context.stack;
    val method = context.thisRef.classDef.findMethod(name);
    val frame = new Frame(context.thisRef, method.block); 

    stack.push(frame);
    
    try {
      if (method.parameters.size != arguments.size) {
        throw new RuntimeException("Method " + method.name + " takes " + method.parameters.size +
            " arguments but " + arguments.size + " were provided");
      }
      for (i <- 0 until arguments.size) {
        var value = arguments(i).evaluate(context) match {
          case Some(v) => v;
          case None => throw new RuntimeException("Something's really wrong");
        }
        val identifier = method.parameters(i).name;

        frame.addIdentifier(identifier, new Tuple2[NoopType, NoopObject](null, value));
      }
      for (statement <- method.block.statements) {
        statement.evaluate(context);
      }
    } finally {
      stack.pop();
    }
    return None;
  }
}