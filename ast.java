import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a bach program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and identifiers contain line and character 
// number information; for string literals and identifiers, they also 
// contain a string; for integer literals, they also contain an integer 
// value.
//
// Here are all the different kinds of AST nodes and what kinds of 
// children they have.  All of these kinds of AST nodes are subclasses
// of "ASTnode".  Indentation indicates further subclassing:
//
//     Subclass              Children
//     --------              --------
//     ProgramNode           DeclListNode
//     DeclListNode          linked list of DeclNode
//     DeclNode:
//       VarDeclNode         TypeNode, IdNode, int
//       FuncDeclNode        TypeNode, IdNode, FormalsListNode, FuncBodyNode
//       FormalDeclNode      TypeNode, IdNode
//       StructDeclNode      IdNode, DeclListNode
//
//     StmtListNode          linked list of StmtNode
//     ExpListNode           linked list of ExpNode
//     FormalsListNode       linked list of FormalDeclNode
//     FuncBodyNode          DeclListNode, StmtListNode
//
//     TypeNode:
//       BooleanNode         --- none ---
//       IntegerNode         --- none ---
//       VoidNode            --- none ---
//       StructNode          IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignExpNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       TrueNode            --- none ---
//       FalseNode           --- none ---
//       IdNode              --- none ---
//       IntLitNode          --- none ---
//       StrLitNode          --- none ---
//       StructAccessNode    ExpNode, IdNode
//       AssignExpNode       ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         EqualsNode
//         NotEqNode
//         LessNode
//         LessEqNode
//         GreaterNode
//         GreaterEqNode
//         AndNode
//         OrNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of children, 
// or internal nodes with a fixed number of children:
//
// (1) Leaf nodes:
//        BooleanNode,  IntegerNode,  VoidNode,    IdNode,  
//        TrueNode,     FalseNode,    IntLitNode,  StrLitNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, StmtListNode, ExpListNode, FormalsListNode
//
// (3) Internal nodes with fixed numbers of children:
//        ProgramNode,     VarDeclNode,      FuncDeclNode,  FormalDeclNode,
//        StructDeclNode,  FuncBodyNode,     StructNode,    AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode,  IfStmtNode,    IfElseStmtNode,
//        WhileStmtNode,   ReadStmtNode,     WriteStmtNode, CallStmtNode,
//        ReturnStmtNode,  StructAccessNode, AssignExpNode, CallExpNode,
//        UnaryExpNode,    UnaryMinusNode,   NotNode,       BinaryExpNode,   
//        PlusNode,        MinusNode,        TimesNode,     DivideNode,
//        EqualsNode,      NotEqNode,        LessNode,      LessEqNode,
//        GreaterNode,     GreaterEqNode,    AndNode,       OrNode
//
// **********************************************************************

// **********************************************************************
//   ASTnode class (base class for all other kinds of nodes)
// **********************************************************************F

abstract class ASTnode {
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k = 0; k < indent; k++)
            p.print(" ");
    }
}

// **********************************************************************
// ProgramNode, DeclListNode, StmtListNode, ExpListNode,
// FormalsListNode, FuncBodyNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    public void nameAnalysis() {
        SymTab symTab = new SymTab();
        myDeclList.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    // 1 child
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    public void nameAnalysis(SymTab symTab) {
        nameAnalysis(symTab, symTab);
    }

    public void nameAnalysis(SymTab symTab, SymTab globalTab) {
        for (DeclNode node : myDecls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode) node).nameAnalysis(symTab, globalTab);
            } else {
                node.nameAnalysis(symTab);
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<DeclNode> it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode) it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of children (DeclNodes)
    private List<DeclNode> myDecls;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void nameAnalysis(SymTab symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // list of children (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public void nameAnalysis(SymTab symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    // list of children (ExpNodes)
    private List<ExpNode> myExps;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    public LinkedList<String> nameAnalysis(SymTab symTab) {
        LinkedList<String> typeList = new LinkedList<String>();
        for (FormalDeclNode node : myFormals) {
            Sym sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
            }
        }
        return typeList;
    }

    public int length() {
        return myFormals.size();
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    // list of children (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FuncBodyNode extends ASTnode {
    public FuncBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void nameAnalysis(SymTab symTab) {
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    // 2 children
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

// **********************************************************************
// ***** DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    abstract public Sym nameAnalysis(SymTab symTab);
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    public Sym nameAnalysis(SymTab symTab) {
        return nameAnalysis(symTab, symTab);
    }

    public Sym nameAnalysis(SymTab symTab, SymTab globalTab) {
        try {
            boolean badDecl = false;
            String name = myId.name();
            Sym sym = null;
            IdNode structId = null;

            if (myType instanceof VoidNode) { // check for void type
                ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                        "Non-function declared void");
                badDecl = true;
            }

            else if (myType instanceof StructNode) {
                structId = ((StructNode) myType).idNode();
                sym = globalTab.lookupGlobal(structId.name());

                // if the name for the struct type is not found,
                // or is not a struct type
                if (sym == null || !(sym instanceof StructDefSym)) {
                    ErrMsg.fatal(structId.lineNum(), structId.charNum(),
                            "Invalid name of struct type");
                    badDecl = true;
                } else {
                    structId.link(sym);
                }
            }

            if (symTab.lookupLocal(name) != null) {
                ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                        "Multiply declared identifier");
                badDecl = true;
            }

            if (!badDecl) { // insert into symbol table

                if (myType instanceof StructNode) {
                    // sym = new StructSym(structId);
                } else {
                    sym = new Sym(myType.type());
                }
                symTab.addDecl(name, sym);
                myId.link(sym);

            }

            return sym;
        } catch (SymDuplicateException ex) {
            System.err.println("Unexpected DuplicateSymException " +
                    " in VarDeclNode.nameAnalysis");
            System.exit(-1);
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in VarDeclNode.nameAnalysis");
            System.exit(-1);
        }
        return null;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(".");
    }

    // 3 children
    private TypeNode myType;
    private IdNode myId;
    private int mySize; // use value NON_STRUCT if this is not a struct type

    public static int NON_STRUCT = -1;
}

class FuncDeclNode extends DeclNode {
    public FuncDeclNode(TypeNode type,
            IdNode id,
            FormalsListNode formalList,
            FuncBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public Sym nameAnalysis(SymTab symTab) {
        String name = myId.name();
        FuncSym sym = null;

        try {
            if (symTab.lookupLocal(name) != null) {
                ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                        "Multiply declared identifier");
            }

            else { // add function name to local symbol table
                try {
                    sym = new FuncSym(myType.type(), myFormalsList.length());
                    symTab.addDecl(name, sym);
                    myId.link(sym);
                } catch (SymDuplicateException ex) {
                    System.err.println("Unexpected DuplicateSymException " +
                            " in FnDeclNode.nameAnalysis");
                    System.exit(-1);
                } catch (SymTabEmptyException ex) {
                    System.err.println("Unexpected EmptySymTableException " +
                            " in FnDeclNode.nameAnalysis");
                    System.exit(-1);
                }
            }
        } catch (SymTabEmptyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        symTab.addScope(); // add a new scope for locals and params

        // process the formals
        LinkedList<String> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
        }

        myBody.nameAnalysis(symTab); // process the function body

        try {
            symTab.removeScope(); // exit scope
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in FnDeclNode.nameAnalysis");
            System.exit(-1);
        }

        return null;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("[");
        myFormalsList.unparse(p, 0);
        p.println("] [");
        myBody.unparse(p, indent + 4);
        p.println("]\n");
    }

    // 4 children
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FuncBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public Sym nameAnalysis(SymTab symTab) {
        String name = myId.name();
        boolean badDecl = false;
        Sym sym = null;

        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                    "Non-function declared void");
            badDecl = true;
        }

        try {
            if (symTab.lookupLocal(name) != null) {
                ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                        "Multiply declared identifier");
                badDecl = true;
            }
        } catch (SymTabEmptyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!badDecl) { // insert into symbol table
            try {
                sym = new Sym(myType.type());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (SymDuplicateException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                        " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (SymTabEmptyException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                        " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        return sym;
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    // 2 children
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public Sym nameAnalysis(SymTab symTab) {
        String name = myId.name();
        boolean badDecl = false;

        try {
            if (symTab.lookupLocal(name) != null) {
                ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                        "Multiply declared identifier");
                badDecl = true;
            }
        } catch (SymTabEmptyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        SymTab structSymTab = new SymTab();

        // process the fields of the struct
        myDeclList.nameAnalysis(structSymTab, symTab);

        if (!badDecl) {
            try { // add entry to symbol table
                StructDefSym sym = new StructDefSym(structSymTab);
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (SymDuplicateException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                        " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (SymTabEmptyException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                        " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        return null;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
        myId.unparse(p, 0);
        p.println(" [");
        myDeclList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("]\n");
    }

    // 2 children
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// **** TypeNode and its subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    abstract public String type();
}

class BooleanNode extends TypeNode {
    public BooleanNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("boolean");
    }

    public String type() {
        return "boolean";
    }
}

class IntegerNode extends TypeNode {
    public IntegerNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("integer");
    }

    public String type() {
        return "integer";
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }

    public String type() {
        return "void";
    }

}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        myId.unparse(p, 0);
    }

    public IdNode idNode() {
        return myId;
    }

    public String type() {
        return myId.toString();
    }

    // 1 child
    private IdNode myId;
}

// **********************************************************************
// **** StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTab symTab);
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignExpNode assign) {
        myAssign = assign;
    }

    public void nameAnalysis(SymTab symTab) {
        myAssign.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(".");
    }

    // 1 child
    private AssignExpNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++.");
    }

    // 1 child
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--.");
    }

    // 1 child
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("}");
    }

    // 3 children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
            StmtListNode slist1, DeclListNode dlist2,
            StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent + 4);
        myThenStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent + 4);
        myElseStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("}");
    }

    // 5 children
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                    " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        doIndent(p, indent);
        p.println("}");
    }

    // 3 children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("input -> ");
        myExp.unparse(p, 0);
        p.println(".");
    }

    // 1 child (actually can only be an IdNode or a StructAccessNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("disp <- (");
        myExp.unparse(p, 0);
        p.println(").");
    }

    // 1 child
    private ExpNode myExp;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    public void nameAnalysis(SymTab symTab) {
        myCall.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(".");
    }

    // 1 child
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void nameAnalysis(SymTab symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(".");
    }

    // 1 child
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// **** ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    public void nameAnalysis(SymTab symTab) {

    }
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("TRUE");
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("FALSE");
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public Sym sym() {
        return mySym;
    }

    public String name() {
        return myStrVal;
    }

    @Override
    public String toString() {
        return myStrVal;
    }

    public int lineNum() {
        return myLineNum;
    }

    public int charNum() {
        return myCharNum;
    }

    public void link(Sym sym) {
        mySym = sym;
    }

    public void nameAnalysis(SymTab symTab) {
        try {
            Sym sym;

            sym = symTab.lookupGlobal(myStrVal);

            if (sym == null) {
                ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
            } else {
                link(sym);
            }
        } catch (SymTabEmptyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);

        if (mySym != null) {
            p.print("{");
            p.print(mySym.toString() + "SOMETHING");
            p.print("}");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym mySym;
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class StructAccessExpNode extends ExpNode {
    public StructAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;
        myId = id;
    }

    public Sym sym() {
        return mySym;
    }

    public int lineNum() {
        return myId.lineNum();
    }

    public int charNum() {
        return myId.charNum();
    }

    public void nameAnalysis(SymTab symTab) {
        badAccess = false;
        SymTab structSymTab = null; // to lookup RHS of dot-access
        Sym sym = null;

        myLoc.nameAnalysis(symTab); // do name analysis on LHS

        // if myLoc is an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode) myLoc;
            sym = id.sym();

            // check ID has been declared to be of a struct type

            if (sym == null) { // ID undeclared
                badAccess = true;
            } else if (sym instanceof StructSym) {
                // get symbol table for struct type
                Sym tempSym = ((StructSym) sym).getStructType().sym();
                structSymTab = ((StructDefSym) tempSym).getSymTable();
            } else { // LHS is not a struct type
                ErrMsg.fatal(id.lineNum(), id.charNum(),
                        "Dot-access of non-struct type");
                badAccess = true;
            }
        }

        // if myLoc is really a dot-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHS id is not of a struct type, or
        // a link to the Sym for the struct type RHSid was declared to be
        else if (myLoc instanceof StructAccessExpNode) {
            StructAccessExpNode loc = (StructAccessExpNode) myLoc;

            if (loc.badAccess) { // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this dot-access
            } else { // no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) { // no struct to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(),
                            "Dot-access of non-struct type");
                    badAccess = true;
                } else { // get the struct's symbol table in which to lookup RHS
                    if (sym instanceof StructDefSym) {
                        structSymTab = ((StructDefSym) sym).getSymTable();
                    } else {
                        System.err.println("Unexpected Sym type in DotAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }

        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }

        // do nameAnalysis on RHS of dot-access in the struct's symbol table
        if (!badAccess) {

            try {
                sym = structSymTab.lookupGlobal(myId.name());
            } catch (SymTabEmptyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } // lookup
            if (sym == null) { // not found - RHS is not a valid field name
                ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                        "Invalid struct field name");
                badAccess = true;
            }

            else {
                myId.link(sym); // link the symbol
                // if RHS is itself as struct type, link the symbol for its struct
                // type to this dot-access node (to allow for chained dot-access)
                if (sym instanceof StructSym) {
                    mySym = ((StructSym) sym).getStructType().sym();
                }
            }
        }
    }

    // **** unparse ****
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);
        p.print("):");
        myId.unparse(p, 0);
    }

    // 2 children
    private ExpNode myLoc;
    private IdNode myId;
    private Sym mySym; // link to Sym for struct type
    private boolean badAccess; // to prevent cascading errors
}

class AssignExpNode extends ExpNode {
    public AssignExpNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void nameAnalysis(SymTab symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)
            p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)
            p.print(")");
    }

    // 2 children
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    public void nameAnalysis(SymTab symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    // 2 children
    private IdNode myId;
    private ExpListNode myExpList; // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }

    // 1 child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    public void nameAnalysis(SymTab symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }

    // 2 children
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// **** Subclasses of UnaryExpNode
// **********************************************************************

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(^");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// **** Subclasses of BinaryExpNode
// **********************************************************************

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" & ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" | ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqNode extends BinaryExpNode {
    public NotEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" ^= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
