# MiniJava Compiler
✔️ For this project I made my own compiler for a language called MiniJava which is a subset of Java. More details about what is permitted in MiniJava are written below. In the file named minijava.html you will find minijava's grammar. This compiler is implemented with the visitor pattern.

## About MiniJava and differences from Java
* MiniJava is fully object-oriented, like Java. It does not allow global functions, only classes, fields and methods. The basic types are int, boolean, and int [] which is an array of int. You can build classes that contain fields of these basic types or of other classes. Classes contain methods with arguments of basic or class types, etc.
* MiniJava supports single inheritance but not interfaces. It does not support function overloading, which means that each method name must be unique. In addition, all methods are inherently polymorphic (i.e., “virtual” in C++ terminology). This means that foo can be defined in a subclass if it has the same return type and argument types (ordered) as in the parent, but it is an error if it exists with other argument types or return type in the parent. Also all methods must have a return type–there are no void methods. Fields in the base and derived class are allowed to have the same names, and are essentially different fields.
* All MiniJava methods are “public” and all fields “protected”. A class method cannot access fields of another class, with the exception of its superclasses. Methods are visible, however. A class’s own methods can be called via “this”. E.g., this.foo(5) calls the object’s own foo method, a.foo(5) calls the foo method of object a. Local variables are defined only at the beginning of a method. A name cannot be repeated in local variables (of the same method) and cannot be repeated in fields (of the same class). A local variable x shadows a field x of the surrounding class.
* In MiniJava, constructors and destructors are not defined. The new operator calls a default void constructor. In addition, there are no inner classes and there are no static methods or fields. By exception, the pseudo-static method “main” is handled specially in the grammar. A MiniJava program is a file that begins with a special class that contains the main method and specific arguments that are not used. The special class has no fields. After it, other classes are defined that can have fields and methods.
Notably, an A class can contain a field of type B, where B is defined later in the file. But when we have “class B extends A”, A must be defined before B. As you’ll notice in the grammar, MiniJava offers very simple ways to construct expressions and only allows < comparisons. There are no lists of operations, e.g., 1 + 2 + 3, but a method call on one object may be used as an argument for another method call. In terms of logical operators, MiniJava allows the logical and (“&&”) and the logical not (“!”). For int arrays, the assignment and [] operators are allowed, as well as the a.length expression, which returns the size of array a. We have “while” and “if” code blocks. The latter are always followed by an “else”. Finally, the assignment “A a = new B();” when B extends A is correct, and the same applies when a method expects a parameter of type A and a B instance is given instead.
* You should only accept expressions of type int as the argument of the PrintStatement.

## Code insights
:mag: My code and my visitors are writted in the file Main.java. SymbolTableVisitor parses the programm and makes the appropriate data structures to hold the variables, functions etc and their scope. That's why it is a big nested hashmap of hashmaps. TypeCheck visitor extends the SymbolTable visitor and copies all it's data. This visitor is used to check the syntax of the programm. Some code insights are also included in my report written in Greek.

## Installation / Run
🔹 Download the compiler and the examples and install them locally.
🔹 Move all the examples from the examples folder you want to run, inside thee 'src' folder. You can also make your own MiniJava programm and compile it, but be careful with MiniJava's syntax.
🔹 Type in the terminal:
```
  $ make compile
```
🔹 Run with:
```
  $ java Main [file1] [file2] ...
```
**[file1] [file2] etc. are the examples files you chose from the examples folder. The programm can run multiple examples at once. <br /><br />
🔹 In the output the variables and the functions are printed together with their memory-offset and scope.

## Built With
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
