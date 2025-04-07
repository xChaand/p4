import java.util.LinkedList;

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
    private LinkedList<String> paramTypes;

    public FuncSym(String returnType) {
        super("function");
        this.returnType = returnType;
    }

    public String toString() {
        // make list of formals *********CHANGE VAR NAMES*************
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
    public StructDefSym() {
        super(null);
    }
}

class StructSym extends Sym { // Instance of a struct
    public StructSym() {
        super(null);
    }
}