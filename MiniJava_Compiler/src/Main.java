import syntaxtree.*;
import visitor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
 
//edw proj
public class Main {
    public static boolean error=false;
    public static void main(String[] args) throws Exception {
        FileInputStream fis = null;
        try{
            for(int i=0; i<args.length ; i++){
                error=false;
                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);
                Goal root = parser.Goal();
                System.err.println("\nProgram parsed successfully.");
                System.out.println("Compiling the programm '"+args[i]+"':");
                SymbolTableVisitor eval = new SymbolTableVisitor();
                root.accept(eval, null);
                TypeCheckVisitor eval_2 = new TypeCheckVisitor(eval);
                root.accept(eval_2, null);

                if(error==true){
                    System.out.println("Compilation failed!");
                    continue;
                }
                else
                    System.out.print("Compilation succesfull!");
                //OFFSET PRINTING
                int j=0;
                System.out.println();
                HashMap<String,List<String>> temp = new HashMap<String,List<String>>();
                List<String> templist = new ArrayList<String>();
                String type;
                int flag=0;
                int is_child;
                String[] splited;
                String parent=null;
                for (String classname: eval.ClassMap.keySet()) {
                    //Main skip
                    if(flag==0){
                        flag=1;
                        continue;
                    }
                    is_child=0;
                    if(eval.Inheritance.containsValue(classname)){
                        is_child=1;
                        for(String str:eval.Inheritance.keySet()){
                            if(classname.equals(eval.Inheritance.get(str)))
                                parent=str;
                        }
                    }
                    if(is_child==0)
                        j=0;
                    System.out.println("-----------Class " + classname + "-----------");
                    System.out.println("---Variables---");
                    templist=eval.ClassVars.get(classname);
                    if(templist==null)
                        continue;
                    for (String var: templist){
                        if(is_child==1 && eval.ClassVars.get(parent).contains(var))
                            continue;
                        splited=var.split(" ");
                        System.out.println(classname+"."+splited[1]+" : "+ j);
                        type=splited[0];
                        if(type.equals("int"))
                            j=j+4;
                        else if(type.equals("boolean"))
                            j=j+1;
                        else
                            j=j+8;
                    }
                    System.out.println("---Methods---");
                    if(is_child==0)
                        j=0;
                    temp=eval.ClassMap.get(classname);
                    if(temp==null)
                        continue;
                    for (String methodname: temp.keySet()){
                        if(is_child==1 && eval.ClassMap.get(parent).containsKey(methodname))
                            continue;
                        System.out.println(classname+"."+methodname +" : "+ j);
                        j=j+8;
                    }
                    System.out.println();
                }
                /* SYMBOL TABLE STRUCTS PRINTING
                //----MAP PRINTING---
                System.out.println();
                for (String classname: eval.ClassMap.keySet()) {
                    System.out.print("<<<" + classname + ">>>"+":");
                    temp=eval.ClassMap.get(classname);
                    for (String methodname: temp.keySet()){
                        System.out.print("{" +methodname +"}"+ ":");
                        templist=temp.get(methodname);
                        System.out.println("Vars:" + templist);
                    }
                }
                //----CLASS VARS PRINTING----
                temp=eval.ClassVars;
                for (String methodname_: temp.keySet()){
                    System.out.print("{" +methodname_ +"}"+ ":");
                    templist=temp.get(methodname_);
                    System.out.println("ClassVars:" + templist);
                }
                //----METHOD ARGS PRINTING----
                temp=eval.MethodArgs;
                for (String methodname__: temp.keySet()){
                    System.out.print("{" +methodname__ +"}"+ ":");
                    templist=temp.get(methodname__);
                    System.out.println("Args:" + templist);
                }
                System.out.println();*/
            }
        }
        catch(ParseException ex){
            System.out.println(ex.getMessage());
        }
        catch(FileNotFoundException ex){
            System.err.println(ex.getMessage());
        }
        finally{
            try{
                if(fis != null) fis.close();
            }
            catch(IOException ex){
                System.err.println(ex.getMessage());
            }
        }
    }
}


class SymbolTableVisitor extends GJDepthFirst<String, String>{
    //These structures of the symbol table are explained in README
    public LinkedHashMap<String,LinkedHashMap<String,List<String>>> ClassMap;
    public LinkedHashMap<String,List<String>> MethodArgs;
    public LinkedHashMap<String,List<String>> ClassVars;
    public LinkedHashMap<String,String> Inheritance;

    SymbolTableVisitor() {
        this.ClassMap=new LinkedHashMap<String,LinkedHashMap<String,List<String>>>();
        this.ClassVars= new LinkedHashMap<String,List<String>>();
        this.MethodArgs= new LinkedHashMap<String,List<String>>();
        this.Inheritance=new LinkedHashMap<String,String>();
    }
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        String[] splited;
        LinkedHashMap<String,List<String>> MethodMap= new LinkedHashMap<String,List<String>>();
        List<String> MethodVars = new ArrayList<String>();
        //Add variables to the main method's variable list
        NodeListOptional varDecls = n.f14;
        for (int i = 0; i < varDecls.size(); ++i) {
            VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
            String varId = varDecl.f1.f0.tokenImage;
            //Here we check if the variable has already been defined
            for(String check: MethodVars){
                splited=check.split(" ");
                if(splited[1].equals(varId)){
                    System.out.println("This variable has already been defined.");
                    Main.error=true;
                }       
            }
            MethodVars.add(get_type(varDecl)+" "+varId);
        }
        MethodMap.put("main", MethodVars);   
        this.ClassMap.put(classname, MethodMap);

        return null;
    }

    /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    @Override
    public String visit(TypeDeclaration n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        String[] splited;
        if(! (this.ClassMap.containsKey(classname))){
            //Add variables to the methods variable list
            List<String> varlist = new ArrayList<String>();
            NodeListOptional varDecls = n.f3;
            for (int i = 0; i < varDecls.size(); ++i) {
                VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
                String varId = varDecl.f1.f0.tokenImage;
                //Here we check if the variable has already been defined
                for(String check: varlist){
                    splited=check.split(" ");
                    if(splited[1].equals(varId)){
                        System.out.println("This variable has already been defined.");
                        Main.error=true;
                    }       
                }
                varlist.add(get_type(varDecl)+" "+varId);
            }
            //Make a new cell in the map with all the nessecary things for the new class
            this.ClassVars.put(classname, varlist);
            LinkedHashMap<String,List<String>> MethodMap = new LinkedHashMap<String,List<String>>();  
            this.ClassMap.put(classname, MethodMap);
            n.f4.accept(this, classname);
            return classname;
        }
        else{
            System.out.println("This class already exists.");
            Main.error=true;
            return classname;
        }
    }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
    @Override 
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception { 
        String flag=" ";
        String classname = n.f1.accept(this, null);
        String parentname = n.f3.accept(this, null);
        String type=" ";
        if(this.ClassMap.containsKey(classname)){
            System.out.println("This class has already been defined.");
            Main.error=true;
            return classname;
        }
        else if(! (this.ClassMap.containsKey(parentname))){
            System.out.println("This class does not exist so it cant be inherited.");
            Main.error=true;
            return classname;
        }
        else{
            String[] splited=null;
            List<String> varlist = new ArrayList<String>();
            NodeListOptional varDecls = n.f5;
            for (int i = 0; i < varDecls.size(); ++i) {
                VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
                String varId = varDecl.f1.f0.tokenImage;
                for(String check: varlist){
                    splited=check.split(" ");
                    if(splited[1].equals(varId)){
                        System.out.println("This variable has already been defined.");
                        throw new Exception();
                    }       
                }
                varlist.add(get_type(varDecl)+" "+varId);
            }
            this.ClassVars.put(classname, varlist);
            LinkedHashMap<String,List<String>> MethodMap = new LinkedHashMap<String,List<String>>();  
            this.ClassMap.put(classname, MethodMap);
            n.f6.accept(this, classname);
            //The above section is the same as ClassDeclaration and now we will add the parent's vars and methods to this class
            LinkedHashMap<String,List<String>> parentmethods  = new LinkedHashMap<String,List<String>>();
            parentmethods=this.ClassMap.get(parentname);
            LinkedHashMap<String,List<String>> curr_methods = new LinkedHashMap<String,List<String>>();
            curr_methods=this.ClassMap.get(classname);
            //Here we make sure we keep the overriden method and not put the parent's one 
            for (String temp: parentmethods.keySet()){
                if (!(curr_methods.containsKey(temp))){
                    flag=null;
                    curr_methods.put(temp, parentmethods.get(temp));
                    //also adding to the MethodArgs the inherited function
                    for(String str:this.MethodArgs.keySet()){
                        splited=str.split(" ");
                        if(splited[1].equals(parentname+"."+temp)){
                            flag=str;
                            type=splited[0];
                        }
                    }
                    if(flag!=null)
                        this.MethodArgs.put(type+" "+classname+"."+temp, this.MethodArgs.get(flag));
                }
                //Overloading and overriding check
                else if(curr_methods.containsKey(temp)){
                    List<String> func_1=new ArrayList<String>();
                    List<String> func_2=new ArrayList<String>();
                    for(String key: this.MethodArgs.keySet()){
                        splited=key.split(" ");
                        if(splited[1].equals(classname+"."+temp))
                            func_1=this.MethodArgs.get(key);
                        if(splited[1].equals(parentname+"."+temp))
                            func_2=this.MethodArgs.get(key);
                    }
                    if (!func_1.equals(func_2)){
                        System.err.println("Function overriding between methods named '"+temp+"'' in class '"+classname+"'");
                        Main.error=true;
                    }
                }
            }
            this.ClassMap.put(classname, curr_methods);
            //vars
            List<String> parentvars = new ArrayList<String>();
            List<String> curr_vars = new ArrayList<String>();
            parentvars=this.ClassVars.get(parentname);
            curr_vars=this.ClassVars.get(classname);
            for (String temp: parentvars){
                if (!(curr_vars.contains(temp)))
                    curr_vars.add(temp);
            }
            this.ClassVars.put(classname, curr_vars);
            //we update the inheritance data struct
            this.Inheritance.put(parentname,classname);
            return classname;
        }
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, String argu) throws Exception {
        String str=new String(n.f0.accept(this, null)+" "+n.f1.accept(this, null));
        return str;
    }

    public String get_type(VarDeclaration n) throws Exception{
        String str = visit(n," ");
        String[] temp = str.split(" ");
        return temp[0];
    }

    
    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, String classname) throws Exception {
        List<String> arglist = new ArrayList<String>();
        String[] splited;
        if (n.f4.present())    
            arglist.add(n.f4.accept(this, classname));   
        String type = n.f1.accept(this, null);
        String name = n.f2.accept(this, null);
        //add arguments to MethodArgs
        this.MethodArgs.put(type+" "+classname+"."+name, arglist); 

        LinkedHashMap<String,List<String>> current_class_methods = this.ClassMap.get(classname);
        
        if(current_class_methods.containsKey(name)){
            System.out.println("This class already has a method named"+" "+name);
            Main.error=true;
            return name;
        }
        else{
            List<String> varlist = new ArrayList<String>();
            //Add variables to the methods variable list
            NodeListOptional varDecls = n.f7;
            for (int i = 0; i < varDecls.size(); ++i) {
                VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
                String varId = varDecl.f1.f0.tokenImage;
                //Here we check if the variable has already been defined
                for(String check: varlist){
                    splited=check.split(" ");
                    if(splited[1].equals(varId)){
                        System.err.println("This variable has already been defined.");
                        Main.error=true;
                    }       
                }
                if(!arglist.isEmpty()){
                    String[] splited_=arglist.get(0).split(",");
                    String[] splited_2;
                    List<String> check=new ArrayList<String>();
                    for(int j=0; j<splited_.length;j++){
                        splited_2=splited_[j].split(" ");
                        check.add(splited_2[1]);
                    }
                    if(check.contains(varId)){
                        System.err.println("Cannot have an argument and a variable with the same name");
                        Main.error=true;
                    }
                    else{
                        varlist.add(get_type(varDecl)+" "+varId);
                        continue;
                    }
                }
                varlist.add(get_type(varDecl)+" "+varId);
            }
            current_class_methods.put(name, varlist);  
            return name;
        }
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception {
        String ret = n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }
        String[] splited=ret.split(",");
        String[] splited_2;
        List<String> check=new ArrayList<String>();
        for(int j=0; j<splited.length;j++){
            splited_2=splited[j].split(" ");
            check.add(splited_2[1]);
        }
        for(int i=0 ; i<check.size()-1 ; i++){
            for(int k=i+1 ; k<check.size() ; k++){
                if(check.get(k).equals(check.get(i))){
                    System.err.println("Cannot have 2 arguments of the same name");
                    Main.error=true;
                }
            }
        }
        return ret;
    }

    /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
    @Override
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
    * f0 -> ( FormalParameterTerm() )*
    */
    @Override
    public String visit(FormalParameterTail n, String argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += "," + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, String argu) throws Exception{
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }

    /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    @Override
    public String visit(Type n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(ArrayType n, String argu) {
        return "int[]";
    }

    @Override
    public String visit(BooleanType n, String argu) {
        return "boolean";
    }

    @Override
    public String visit(IntegerType n, String argu) {
        return "int";
    }

    @Override
    public String visit(Identifier n, String argu) {
        return n.f0.toString();
    }
    
}

class TypeCheckVisitor extends SymbolTableVisitor{
    
    TypeCheckVisitor(SymbolTableVisitor S){
        this.ClassMap=new LinkedHashMap<String,LinkedHashMap<String,List<String>>>();
        this.ClassVars= new LinkedHashMap<String,List<String>>();
        this.MethodArgs= new LinkedHashMap<String,List<String>>();
        this.ClassMap=S.ClassMap;
        this.MethodArgs=S.MethodArgs;
        this.ClassVars=S.ClassVars;
        this.Inheritance=S.Inheritance;
    }
    /**
     *  f0 -> "class"
     * f1 -> Identifier()
     * f15 -> ( Statement() )*
     */
    @Override
    public String visit(MainClass n, String argu) throws Exception {
        n.f15.accept(this, n.f1.accept(this, argu)+ " main");
        return null;
    }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, argu);
        n.f4.accept(this, classname);
        return null;
    }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
    @Override 
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception { 
        String classname = n.f1.accept(this, argu);
        n.f6.accept(this, classname);
        return null;
    }

    /**
     * f8 -> ( Statement() )*
     * f10 -> Expression()
     */
    @Override
    public String visit(MethodDeclaration n, String classname) throws Exception {
        String methodname=n.f2.accept(this, classname);
        n.f8.accept(this, classname+ " " + methodname);
        //Typecheck for the return type
        String return_type=n.f10.accept(this, classname+ " " + methodname);
        if(!return_type.equals("boolean") && !return_type.equals("int") && !return_type.equals("allocation") && !return_type.equals("arrayallocation") && !return_type.equals("this"))
            if(!(this.ClassMap.containsKey(return_type)))
                return_type=this.var_exists(classname+ " " + methodname, return_type);
        if(return_type.equals("this"))
            return_type=classname;
        String type=this.getMethodType(methodname, classname);
        if(! (return_type.equals(type))){
            if(!this.Inheritance.containsKey(type)){
                if(!this.Inheritance.get(type).equals(return_type)){
                System.err.println("The return type of "+methodname+" must be of type "+type+" and not "+return_type);
                Main.error=true;
                }
            }
        }
        return null;
    }
    /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
    @Override
    public String visit(Statement n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
    @Override
    public String visit(Block n, String argu) throws Exception {
        return  n.f1.accept(this, argu);
    }

    /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    @Override
    public String visit(AssignmentStatement n, String str) throws Exception {
        String[] splited;
        splited=str.split(" ");
        String classname = splited[0];
        //String method = splited[1];
        String var = n.f0.accept(this, str);
        String type=this.var_exists(str, var);
        String exprtype=n.f2.accept(this,str);
        if(!exprtype.equals("boolean") && !exprtype.equals("int") && !exprtype.equals("allocation") && !exprtype.equals("arrayallocation") && !exprtype.equals("this")){
            if(!(this.ClassMap.containsKey(exprtype)))
                exprtype=this.var_exists(str, exprtype);
        }
        if (type.equals("int[]") && exprtype.equals("arrayallocation"))
            return "ok";
        if(!type.equals("boolean") && !type.equals("int") && !type.equals("allocation") && !type.equals("arrayallocation") && !type.equals("this")){
            //Below there is code to solve errors like:
            //  Tree t;
            //  t=new RandomClass();
            if(exprtype.equals("allocation")){
                String temp=n.f2.accept(this,"assign");
                if(!temp.equals(type)){
                    System.err.println("Type missmatch: cannot convert from "+temp+" to "+type);
                    Main.error=true;
                }
                return "ok";
            }
        }
        if(exprtype.equals("this"))
            exprtype=classname;
        if(!(type.equals(exprtype))){
            System.err.println("Wrong assignment between "+type+" and "+exprtype);
            Main.error=true;
        }
        return " ";
    }

    /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
    @Override
    public String visit(ArrayAssignmentStatement n, String str) throws Exception {
        String[] splited;
        splited=str.split(" ");
        String classname = splited[0];
        String method = splited[1];
        String var = n.f0.accept(this, str);
        //Checking if the var belongs to the method
        String type="default";
        boolean flag=false;
        LinkedHashMap<String,List<String>> temp = new LinkedHashMap<String,List<String>>();
        List<String> templist = new ArrayList<String>();
        temp=this.ClassMap.get(classname);
        templist=temp.get(method);
        for (String methodvars: templist){
            splited=methodvars.split(" ");
            if (var.equals(splited[1])){
                type=splited[0];
                flag=true;
                break;
            }
        }
        //If it is not in the method vars then it might be in class vars
        if(flag==false){
            templist=this.ClassVars.get(classname);
            for (String classvars: templist){
                splited=classvars.split(" ");
                if (var.equals(splited[1])){
                    type=splited[0];
                    flag=true;
                    break;
                }
            }
        }
        if(flag==false){
            System.err.println("The variable "+var+" does not exists");
            Main.error=true;
        }
        if (!(type.equals("int[]"))){
            System.err.println("This variable is not an array.");
            Main.error=true;
        }
        //Array cell expression
        String exprtype=n.f2.accept(this,str);
        if(!exprtype.equals("boolean") && !exprtype.equals("int") && !exprtype.equals("allocation") && !exprtype.equals("arrayallocation") && !exprtype.equals("this"))
            if(!(this.ClassMap.containsKey(exprtype)))
                exprtype=this.var_exists(str, exprtype);
        if(!(exprtype.equals("int")) && !(exprtype.equals("int[]"))){
            System.err.println("Array cell expression must be of type int and not "+exprtype);
            Main.error=true;
        }
        //Array assignment expression
        exprtype=n.f5.accept(this,str);
        if(!exprtype.equals("boolean") && !exprtype.equals("int") && !exprtype.equals("allocation") && !exprtype.equals("arrayallocation") && !exprtype.equals("this"))
            if(!(this.ClassMap.containsKey(exprtype)))
                exprtype=this.var_exists(str, exprtype);
        if(!(exprtype.equals("arrayallocation")) && !(exprtype.equals("int")) && !(exprtype.equals("int[]"))){
            System.err.println("Wrong assignment between int[] and "+exprtype);
            Main.error=true;
        }
        return "ok";
    }

    /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    @Override
    public String visit(IfStatement n, String argu) throws Exception{
        String expr_type=n.f2.accept(this, argu);
        if(expr_type.equals("int") || expr_type.equals("allocation") || expr_type.equals("arrayallocation") || expr_type.equals("this")){
            System.err.println("Type mismatch: 'if' expected boolean instead of "+expr_type);
            Main.error=true;
        }
        else if(expr_type.equals("boolean")){
            n.f4.accept(this,argu);
            n.f6.accept(this,argu);
        }
        else if (this.var_exists(argu, expr_type).equals("boolean")){
            n.f4.accept(this,argu);
            n.f6.accept(this,argu);
        }
        else{
            System.err.println("Type mismatch: 'if' expected boolean instead of "+expr_type);
            Main.error=true;
        }
        return " ";
    }

    /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    @Override
    public String visit(WhileStatement n, String argu) throws Exception{
        String expr_type=n.f2.accept(this, argu);
        if(expr_type.equals("int") || expr_type.equals("allocation") || expr_type.equals("arrayallocation") || expr_type.equals("this")){
            System.err.println("Type mismatch: 'while' statement condition expected boolean instead of "+expr_type);
            Main.error=true;
        }
        //while expression must be boolean
        else if(expr_type.equals("boolean"))
            n.f4.accept(this,argu);
        //if its not boolean it might be a boolean identifier so we check this too
        else if (this.var_exists(argu, expr_type).equals("boolean"))
            n.f4.accept(this,argu);
        else{
            System.err.println("Type mismatch: 'while' statement condition expected boolean instead of "+expr_type);
            Main.error=true;
        }
        return " ";
    }

    /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    @Override
    public String visit(PrintStatement n, String argu) throws Exception{
        String exprtype=n.f2.accept(this, argu);
        //If type=allocation or object its an error
        if(exprtype.equals("allocation") || exprtype.equals("arrayallocation") || exprtype.equals("this") || this.ClassMap.containsKey(exprtype)){
            System.err.println("Print error, wrong variable type");
            Main.error=true;
        }
        //Identifier check here
        if(!exprtype.equals("boolean") && !exprtype.equals("int"))
            exprtype=this.var_exists(argu, exprtype);
        if(exprtype.equals("allocation") || exprtype.equals("arrayallocation") || exprtype.equals("this") || this.ClassMap.containsKey(exprtype)){
            System.err.println("Print error: wrong variable type");
            Main.error=true;
        }
        return "ok";
    }

    @Override
    public String visit(Expression n, String argu) throws Exception{
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    @Override
    public String visit(AndExpression n, String argu) throws Exception{
        String str=n.f0.accept(this,argu);
        String str_2=n.f2.accept(this,argu);
        //The  3 'if's are for literal and raw variables, the last for identifiers
        if(str.equals("boolean") && str_2.equals("boolean"))
            return "boolean";
        else if(str.equals("int") || str.equals("allocation") || str.equals("arrayallocation") || str.equals("this")){
            System.err.println("And operation must be between booleans");
            Main.error=true;
            return "boolean";
        }
        else if(str_2.equals("int") || str_2.equals("allocation") || str_2.equals("arrayallocation") || str_2.equals("this")){
            System.err.println("And operation must be between booleans");
            Main.error=true;
            return "boolean";
        }
        else {
            String type=str;
            String type_2=str_2;
            if(!str.equals("boolean"))
                type=this.var_exists(argu, str);
            if(!str_2.equals("boolean"))
                type_2=this.var_exists(argu, str_2);
            if(type.equals("boolean") && type_2.equals("boolean"))
                return "boolean";
            else{
                System.err.println("And operation must be between booleans");
                Main.error=true;
                return "boolean";
            }
        } 
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(CompareExpression n, String argu) throws Exception{
        //We follow the same logic as in the AndExpression here and for the arithmetic expressions below too.
        String var_1=n.f0.accept(this,argu);
        String var_2=n.f2.accept(this,argu);
        if(var_1.equals("int") && var_2.equals("int"))
            return "boolean";
        else if(var_1.equals("boolean") || var_1.equals("allocation") || var_1.equals("arrayallocation") || var_1.equals("this")){
            System.err.println("Compare operation must be between booleans");
            Main.error=true;
            return "boolean";
        }
        else if(var_2.equals("boolean") || var_2.equals("allocation") || var_2.equals("arrayallocation") || var_2.equals("this")){
            System.err.println("Compare operation must be between booleans");
            Main.error=true;
            return "boolean";
        }
        else {
            String type=var_1;
            String type_2=var_2;
            if(!var_1.equals("int"))
                type=this.var_exists(argu, var_1);
            if(!var_2.equals("int"))
                type_2=this.var_exists(argu, var_2);
            if(type.equals("int") && type_2.equals("int"))
                return "boolean";
            else{
                System.err.println("Compare operation must be between booleans");
                Main.error=true;
                return "boolean";
            }
        } 
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(PlusExpression n, String argu) throws Exception{
        String var_1=n.f0.accept(this,argu);
        String var_2=n.f2.accept(this,argu);
        if(var_1.equals("int") && var_2.equals("int"))
            return "int";
        else if(var_1.equals("boolean") || var_1.equals("allocation") || var_1.equals("arrayallocation") || var_1.equals("this")){
            System.err.println("Plus operation must be between integers");
            Main.error=true;
            return "int";
        }
        else if(var_2.equals("boolean") || var_2.equals("allocation") || var_2.equals("arrayallocation") || var_2.equals("this")){
            System.err.println("Plus operation must be between integers");
            Main.error=true;
            return "int";
        }
        else {
            String type=var_1;
            String type_2=var_2;
            if(!var_1.equals("int"))
                type=this.var_exists(argu, var_1);
            if(!var_2.equals("int"))
                type_2=this.var_exists(argu, var_2);
            if(type.equals("int") && type_2.equals("int"))
                return "int";
            else{
                System.err.println("Plus operation must be between integers");
                Main.error=true;
                return "int";
            }
        } 
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(MinusExpression n, String argu) throws Exception{
        String var_1=n.f0.accept(this,argu);
        String var_2=n.f2.accept(this,argu);
        if(var_1.equals("int") && var_2.equals("int"))
            return "int";
        else if(var_1.equals("boolean") || var_1.equals("allocation") || var_1.equals("arrayallocation") || var_1.equals("this")){
            System.err.println("Minus operation must be between integers");
            Main.error=true;
            return "int";
        }
        else if(var_2.equals("boolean") || var_2.equals("allocation") || var_2.equals("arrayallocation") || var_2.equals("this")){
            System.err.println("Minus operation must be between integers");
            Main.error=true;
            return "int";
        }
        else {
            String type=var_1;
            String type_2=var_2;
            if(!var_1.equals("int"))
                type=this.var_exists(argu, var_1);
            if(!var_2.equals("int"))
                type_2=this.var_exists(argu, var_2);
            if(type.equals("int") && type_2.equals("int"))
                return "int";
            else{
                System.err.println("Minus operation must be between integers");
                Main.error=true;
                return "int";
            }
        } 
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(TimesExpression n, String argu) throws Exception{
        String var_1=n.f0.accept(this,argu);
        String var_2=n.f2.accept(this,argu);
        if(var_1.equals("int") && var_2.equals("int"))
            return "int";
        else if(var_1.equals("boolean") || var_1.equals("allocation") || var_1.equals("arrayallocation") || var_1.equals("this")){
            System.err.println("Times operation must be between integers");
            Main.error=true;
            return "int";
        }
        else if(var_2.equals("boolean") || var_2.equals("allocation") || var_2.equals("arrayallocation") || var_2.equals("this")){
            System.err.println("Times operation must be between integers");
            Main.error=true;
            return "int";
        }
        else {
            String type=var_1;
            String type_2=var_2;
            if(!var_1.equals("int"))
                type=this.var_exists(argu, var_1);
            if(!var_2.equals("int"))
                type_2=this.var_exists(argu, var_2);
            if(type.equals("int") && type_2.equals("int"))
                return "int";
            else{
                System.err.println("Times operation must be between integers");
                Main.error=true;
                return "int";
            }
        } 
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    @Override
    public String visit(ArrayLookup n, String argu) throws Exception{
        //First we check if the array exists in the symbol table as a variable
        String var_1=n.f0.accept(this,argu);
        String type=this.var_exists(argu, var_1);
        if(!(type.equals("int[]"))){
            System.err.println("Type mismatch: expected int[] insted of "+type);
            Main.error=true;
        }
        //Then the expression must be of type int
        String var_2=n.f2.accept(this,argu);
        if(var_2.equals("int"))
            return "int";
        else if(var_2.equals("boolean") || var_2.equals("allocation") || var_2.equals("arrayallocation") || var_2.equals("this")){
            System.err.println("Type mismatch: array lookup variable must be of type int");
            Main.error=true;
            return "int";
        }
        else {
            String type_2=this.var_exists(argu, var_2);
            if(type_2.equals("int")){
                return "int";
            }
            else{
                System.err.println("Type mismatch: array lookup variable must be of type int");
                Main.error=true;
                return "int";
            }
        } 
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
    public String visit(ArrayLength n, String argu) throws Exception{
        //First we check if the array exists in the symbol table as a variable
        String var_1=n.f0.accept(this,argu);
        String type=this.var_exists(argu, var_1);
        //Then we check if it is an int[]
        if(!(type.equals("int[]"))){
            System.err.println("Type mismatch: expected int[] insted of "+type);
            Main.error=true;
        }
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    @Override
    public String visit(MessageSend n, String argu) throws Exception{
        //The next 18 lines we are searching if the class and her method exist and we find the mehtods return type
        String[] splited=null;
        String[] expr=null;
        String return_type=null;
        String classname = n.f0.accept(this,argu+",message");
        splited=argu.split(",");
        argu=splited[0];
        if(classname.equals("this")){
            splited=argu.split(" ");
            classname=splited[0];
        }
        String method = n.f2.accept(this,argu);
        if(!(this.ClassMap.containsKey(classname)))
            classname=var_exists(argu, classname);
        LinkedHashMap<String,List<String>> methods = new LinkedHashMap<String,List<String>>();
        methods=this.ClassMap.get(classname);
        if(!(methods.containsKey(method))){
            System.err.println("There is no method named "+method+" in class "+classname);
            Main.error=true;
            return return_type;
        }
        for(String temp: this.MethodArgs.keySet()){
            splited=temp.split(" ");
            if(splited[1].equals(classname+"."+method)){
                return_type = splited[0];
                break;
            }
        }
        //Now we are doing argument checking
        if(n.f4.present()){
            List<String> exprlist = new ArrayList<String>();
            List<String> arglist = new ArrayList<String>();
            exprlist.add(n.f4.accept(this,argu));
            String[] expressions=exprlist.get(0).split(",");
            arglist=this.MethodArgs.get(return_type+" "+classname+"."+method);
            String[] arg_array=arglist.get(0).split(",");
            if(arg_array.length!=expressions.length){
                System.err.println("Mismatched number of arguments in function call '"+method+"'");
                Main.error=true;
                return return_type;
            }
            int i=0;
            for (String arg: arg_array){
                splited=arg.split(" ");
                expr=expressions[i].split(" ");
                if(!(splited[0].equals(expr[0]))){
                    //If the following is true it means we use a class by her parent's name which is allowed
                    if(this.Inheritance.containsKey(splited[0]) && (this.Inheritance.get(splited[0]).equals(expr[0]))){
                        i++;
                        continue;
                    }
                    System.err.println("The number "+(i+1)+" argument in method '"+ method+"' is wrong.");
                    Main.error=true;
                }
                i++;
            }
        }
        return return_type;
    }

    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    @Override
    public String visit(ExpressionList n, String argu) throws Exception {
        String ret = n.f0.accept(this, argu);
        String[] splited;
        splited=argu.split(" ");
        String classname = splited[0];
        if(ret.equals("this"))
            ret=classname;
        //if ret is in the classmap then it means it has a custom type so nothing must be done
        else if(this.ClassMap.containsKey(ret) || ret.equals("int") || ret.equals("boolean") || (ret.equals("int[]")))
            ;
        //Last option is to be an identifier so we must get the type of that identifier (int,boolean etc)
        else
          ret=var_exists(argu, ret);
        if (n.f1 != null) 
            ret += n.f1.accept(this, argu);
        return ret;
    }

    /**
    * f0 -> ( ExpressionTerm() )*
    */
    @Override
    public String visit(ExpressionTail n, String argu) throws Exception {
        String ret = "";
        for (Node node: n.f0.nodes) {
            ret += "," + node.accept(this, argu);
        }
        return ret;
    }

    /**
    * f0 -> ","
    * f1 -> Expression()
    */
    @Override
    public String visit(ExpressionTerm n, String argu) throws Exception {
        String ret=n.f1.accept(this, argu);
        String[] splited;
        splited=argu.split(" ");
        String classname = splited[0];
        if(ret.equals("this"))
            ret=classname;
        //if ret is in the classmap then it means it has a custom type so nothing must be done
        else if(this.ClassMap.containsKey(ret) || ret.equals("int") || ret.equals("boolean") || (ret.equals("int[]")))
            ;
        //Last option is to be an identifier so we must get the type of that identifier (int,boolean etc)
        else
          ret=var_exists(argu, ret);
        return ret;
    }

    /**
    * f0 -> NotExpression()
    *       | PrimaryExpression()
    */
    @Override
    public String visit(Clause n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
    */
    @Override
    public String visit(PrimaryExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
    * f0 -> <INTEGER_LITERAL>
    */
    @Override
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return "int";
    }

    /**
    * f0 -> "true"
    */
    @Override
    public String visit(TrueLiteral n, String argu) throws Exception {
        return "boolean";
    }

    /**
    * f0 -> "false"
    */
    @Override
    public String visit(FalseLiteral n, String argu) throws Exception {
        return "boolean";
    }


    @Override
    public String visit(Identifier n, String argu) {
        return n.f0.toString();
    }

    /**
    * f0 -> "this"
    */
    @Override
    public String visit(ThisExpression n, String argu) throws Exception {
        return "this";
    }

    /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    @Override
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        String str=n.f3.accept(this,argu);
        if(str.equals("int"))
            return "arrayallocation";
        else if(str.equals("bool") || str.equals("allocation") || str.equals("arrayallocation") || str.equals("this")){
            System.err.println("Array allocation variable must be of type int.");
            Main.error=true;
            return "arrayallocation";
        }
        else {
            String type=this.var_exists(argu, str);
            if(type.equals("int")){
                return "arrayallocation";
            }
            else{
                System.err.println("Array allocation variable must be of type int.");
                Main.error=true;
                return "arrayallocation";
            }
        } 
    }

    /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    @Override
    public String visit(AllocationExpression n, String argu) throws Exception {
        String[] splited=null;
        String classname=n.f1.accept(this,argu);
        if(!(this.ClassMap.containsKey(classname))){
            System.err.println("The type of object you are trying to allocate does not exist.");
            Main.error=true;
        }
        splited=argu.split(",");
        if(splited.length>1){
            if(splited[1].equals("message"))
                return classname;
        }
        //if argu=assign is a special case to solve errors like:
        //  Tree t;
        //  t=new RandomClass();
        if(argu.equals("assign"))
            return classname;
        else
            return "allocation";
    }

    /**
    * f0 -> "!"
    * f1 -> Clause()
    */
    @Override
    public String visit(NotExpression n, String argu) throws Exception {
        String str=n.f1.accept(this,argu);
        if(str.equals("boolean"))
            return "boolean";
        else if(str.equals("int") || str.equals("allocation") || str.equals("arrayallocation") || str.equals("this")){
            System.err.println("Not expression must be applied to a bool variable");
            Main.error=true;
            return "boolean";
        }
        else {
            String type=this.var_exists(argu, str);
            if(type.equals("boolean")){
                return "boolean";
            }
            else{
                System.err.println("Not expression must be applied to a bool variable");
                Main.error=true;
                return "boolean";
            }
        }  
    }

    /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
    public String visit(BracketExpression n, String argu) throws Exception {
        return n.f1.accept(this,argu);
    }

    //This utility function takes a method as an argument and returns the return type of that method
    public String getMethodType(String name,String classname){
        String[] splited;
        for(String key: this.MethodArgs.keySet()){
            splited=key.split(" ");
            if(splited[1].equals(classname+"."+name))
                return splited[0];
        }
        System.out.println("There is no method named "+name);
        return "ok";
    }

    //This utility function looks at the method vars and class vars in which an expression belongs and if the var we are analyzing exists in 1 of these 
    //it returns it's type or else it returns "unknown and prints error"
    public String var_exists(String class_and_method,String var){
        String[] splited;
        splited=class_and_method.split(" ");
        String classname = splited[0];
        String method = splited[1];
        //Checking if the var belongs to the method
        String type="unknown";
        boolean flag=false;
        LinkedHashMap<String,List<String>> temp = new LinkedHashMap<String,List<String>>();
        List<String> templist = new ArrayList<String>();
        temp=this.ClassMap.get(classname);
        templist=temp.get(method);
        if(templist!=null){
            for (String methodvars: templist){
                splited=methodvars.split(" ");
                if (var.equals(splited[1])){
                    type=splited[0];
                    flag=true;
                    break;
                }
            }
        }
        //Checking if the var belongs to the method's args
        if(flag==false && templist!=null){
            String[] splited_2=null;
            templist=this.MethodArgs.get(this.getMethodType(method, classname)+" "+classname+"."+method);
            for (String methodargs: templist){
                splited=methodargs.split(",");
                for(String splited_:splited){
                    splited_2=splited_.split(" ");
                    if (var.equals(splited_2[1])){
                        type=splited_2[0];
                        flag=true;
                        break;
                    }
                }
            }
        }
        //If it is not in the method vars or method args then it might be in class vars
        if(flag==false){
            templist=this.ClassVars.get(classname);
            for (String classvars: templist){
                splited=classvars.split(" ");
                if (var.equals(splited[1])){
                    type=splited[0];
                    flag=true;
                    break;
                }
            }
        }
        if(flag==false){
            System.err.println("The variable "+var+" does not exist");
            Main.error=true;
            return var;
        }
        return type;
    }
}