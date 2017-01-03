public class LetInfo {
    String name;
    String type;
    AST expr;

    LetInfo(){
        // default constructor
    }
    LetInfo(String name, String type, AST expr){
        this.name = name;
        this.type = type;
        this.expr = expr;
    }
}
