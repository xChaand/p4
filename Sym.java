import java.util.LinkedList;
import java.util.List;

public class Sym { // Variable declarations
    private String type;

    public Sym(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return type;
    }
}

class FuncSym extends Sym { // Function declarations

    private String returnType;
    private int numParams;
    private LinkedList<String> paramTypes;

    public FuncSym(String type, int numparams) {
        super("function");
        returnType = type;
        numParams = numparams;
    }

    public void addFormals(LinkedList<String> L) {
        paramTypes = L;
    }

    public String getReturnType() {
        return returnType;
    }

    public int getNumParams() {
        return numParams;
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public String toString() {
        // make list of formals
        String str = "";
        boolean notfirst = false;
        for (String type : paramTypes) {
            if (notfirst)
                str += ",";
            else
                notfirst = true;
            str += type.toString();
        }

        str += "->" + returnType.toString();
        return str;
    }
}

class StructDefSym extends Sym { // Subclass for struct type definitions
    private SymTab symTab;

    public StructDefSym(SymTab table) {
        super("struct-def");
        symTab = table;
    }

    public SymTab getSymTable() {
        return symTab;
    }
}

class StructSym extends Sym { // Instance of a struct
    private IdNode structType; // name of the struct type

    public StructSym(IdNode id) {
        super("struct-decl");
        structType = id;
    }

    public IdNode getStructType() {
        return structType;
    }
}