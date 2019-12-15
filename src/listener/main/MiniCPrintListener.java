package listener.main;
import generated.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

//201402318 권순창

public class MiniCPrintListener extends MiniCBaseListener{
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<>();
    int count = 0; //nesting되어 들어갈때 들여쓰는 횟수

    public String stamp() { //nesting된 횟수만큼 dot 찍는 함수
        String Stamp = "....";
        String empty = "";
        for(int i = 0; i < count; i++) {
            empty += Stamp;
        }
        return empty;
    }

    boolean isBinaryOperation(MiniCParser.ExprContext ctx) { //이항연산자인지 확인
        return ctx.getChild(0) != ctx.IDENT() && ctx.getChildCount() ==3 && ctx.getChild(1) != ctx.expr();
    }

    @Override
    public void exitProgram(MiniCParser.ProgramContext ctx) { //프로그램이 끝날때 newTexts에 key가 ctx.decl(i)로 등록되어있는 value를 출력
        for(int i = 0; i < ctx.decl().size(); i++) {
            System.out.println(newTexts.get(ctx.decl(i)));
        }
    }

    @Override
    public void exitDecl(MiniCParser.DeclContext ctx) { //선언이 변수인지 함수인지 확인후 newTexts에 put
        if (ctx.getChild(0) == ctx.var_decl()) {//변수일떄
            newTexts.put(ctx, newTexts.get(ctx.var_decl()));
        }
        else if(ctx.getChild(0) == ctx.fun_decl()) { // 함수일떄
            newTexts.put(ctx, newTexts.get(ctx.fun_decl()));
        }
    }

    @Override
    public void exitVar_decl(MiniCParser.Var_declContext ctx) { //변수선언일때 변수에 맞게 newTexts에 put
        String a1 = newTexts.get(ctx.type_spec()); //타입
        String a2 = ctx.IDENT().getText(); //변수이름
        String a3 = ctx.getChild(2).getText();
        if(ctx.getChildCount() == 3) { //그냥 변수만선언
            newTexts.put(ctx, stamp() + a1 + " " + a2 + a3);
        }
        else if(ctx.getChildCount() == 5) { //변수에 값까지 선언
            String a4 = ctx.LITERAL().getText(); //값
            String a5 = ctx.getChild(4).getText();
            newTexts.put(ctx, stamp() + a1 + " " + a2 + " " + a3 + " " + a4 + a5);
        }
        else if(ctx.getChildCount() == 6) { //배열선언
            String a4 = ctx.LITERAL().getText(); //배열 크기
            String a5 = ctx.getChild(4).getText();
            String a6 = ctx.getChild(5).getText();
            newTexts.put(ctx, stamp() + a1 + " " + a2 + a3 + a4 + a5 + a6);
        }
    }

    @Override
    public void exitType_spec(MiniCParser.Type_specContext ctx) { //타입 선언일때 newTexts에 put
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void enterFun_decl(MiniCParser.Fun_declContext ctx) { //함수면 nesting되므로 count++
        count++;
    }

    @Override
    public void exitFun_decl(MiniCParser.Fun_declContext ctx) { //함수 선언일떄 newTexts에 put
        count--; //함수가 끝났으므로 count--
        String a1 = newTexts.get(ctx.type_spec()); //타입
        String a2 = ctx.IDENT().getText(); //함수이름
        String a3 = ctx.getChild(2).getText();
        String a4 = newTexts.get(ctx.params()); //파라미터
        String a5 = ctx.getChild(4).getText();
        String a6 = newTexts.get(ctx.compound_stmt()); //함수안의 stmt
        newTexts.put(ctx, stamp() + a1 + " " + a2 + a3 + a4 + a5 + "\n" + stamp() + "{\n" + a6 + stamp() + "}\n");
    }

    @Override
    public void exitParams(MiniCParser.ParamsContext ctx) { //매개변수들을 newTexts에 삽입
        if (ctx.getChildCount() == 0) { //파라미터가 없을때
            newTexts.put(ctx, "");
        }
        else if (ctx.getChild(0) == ctx.VOID()) { //함수 타입이 void일때
            newTexts.put( ctx, "");
        }
        else {
            String a1 = newTexts.get(ctx.param(0)); //최소 한개는 나오니 추가
            for(int i = 1; i < ctx.param().size(); i++) { //그이상일때 삽입
                a1 = a1 +", " + newTexts.get(ctx.param(i));
            }
            newTexts.put(ctx, a1);
        }
    }

    @Override
    public void exitParam(MiniCParser.ParamContext ctx) { //각 매개변수를 newTexts에 삽입
        String a1 = newTexts.get(ctx.type_spec()); //매개변수 타입
        String a2 = ctx.IDENT().getText(); //매개변수 이름
        if(ctx.getChildCount() == 2) {
            newTexts.put(ctx, a1 + " " + a2);
        }
        else { //배열인경우
            String a3 = ctx.getChild(2).getText();
            String a4 = ctx.getChild(3).getText();
            newTexts.put(ctx, a1 + " " + a2 + a3 + a4);
        }
    }

    @Override
    public void exitStmt(MiniCParser.StmtContext ctx) { //Stmt가 끝났을때 해당 객체의 자식을 newTexts에 삽입
        newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
    }

    @Override
    public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) { //Expr_stmt가 끝났을때 Expr과 ;붙여서 newTexts에 삽입
        newTexts.put(ctx, stamp() + newTexts.get(ctx.expr()) + ctx.getChild(1).getText() + "\n");
    }

    @Override
    public void enterWhile_stmt(MiniCParser.While_stmtContext ctx) { //while문 들어갈때 nesting이 되므로 count++
        count++;
    }

    @Override
    public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) { //while문 끝날때
        count--; //while문을 빠져나왔으므로 count--
        String a1 = ctx.WHILE().getText(); // while
        String a2 = ctx.getChild(1).getText(); // (
        String a3 = newTexts.get(ctx.expr()); // expr
        String a4 = ctx.getChild(3).getText(); // )
        String a5 = newTexts.get(ctx.stmt()); // stmt
        newTexts.put(ctx, stamp() + a1 + " " + a2 + a3 + a4 + "\n" + stamp() + "{\n" + a5 + stamp() + "}\n"); //nest된 횟수에 맞게 stamp찍으면서 newTexts에 삽입
    }

    @Override
    public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) { //함수의 실행부분
        String a1 = "";
        String a2 = "";
        for(int i = 0; i < ctx.local_decl().size(); i++) { //지역변수 선언
            a1 = a1 + newTexts.get(ctx.local_decl(i));
        }
        for(int i = 0; i < ctx.stmt().size(); i++) { //stmt부분
            a2 = a2 + newTexts.get(ctx.stmt(i));
        }
        newTexts.put(ctx, a1 + a2); //newTexts에 삽입
    }

    @Override
    public void exitLocal_decl(MiniCParser.Local_declContext ctx) { //지역변수선언
        String a1= newTexts.get(ctx.type_spec()); //변수 타입
        String a2 = ctx.IDENT().getText(); //변수 이름
        String a3 = ctx.getChild(2).getText();
        if (ctx.getChildCount() == 3) { //변수만 선언했을떄
            newTexts.put(ctx, stamp() + a1 + " " + a2 + a3 + "\n");
        }
        else if(ctx.getChildCount() == 5) { //값을같이 선언했을떄
            String a4 = ctx.LITERAL().getText(); //값
            String a5 = ctx.getChild(4).getText();
            newTexts.put(ctx, stamp() + a1 + " " + a2 +" "+ a3 + " " + a4 + a5 + "\n");
        }
        else if(ctx.getChildCount() == 6) { //변수가 배열일때
            String a4 = ctx.LITERAL().getText();//배열크기
            String a5 = ctx.getChild(4).getText();
            String a6 = ctx.getChild(5).getText();
            newTexts.put(ctx, stamp() + a1 + " " + a2 + a3 + a4 + a5 + a6 + "\n");
        }
    }

    @Override
    public void enterIf_stmt(MiniCParser.If_stmtContext ctx) { //if문 들어갈떄 nesting되니 count++
        count++;
    }

    @Override
    public void exitIf_stmt(MiniCParser.If_stmtContext ctx) { //if문
        count--; //if문 나올때 count감소
        String a1 = ctx.IF().getText(); //if
        String a2 = ctx.getChild(1).getText(); //(
        String a3 = newTexts.get(ctx.expr()); //expr
        String a4 = ctx.getChild(3).getText(); //)
        String a5 = newTexts.get(ctx.stmt(0)); //stmt
        if(ctx.getChildCount() == 5) { //그냥 if문만있을때 newTexts에 삽입
            newTexts.put(ctx, stamp() + a1 + " " + a2 + a3 + a4 + "\n" + stamp() + "{\n"+ a5 + stamp() + "}\n");
        }
        else {
            String a6 = ctx.ELSE().getText(); //else
            String a7 = newTexts.get(ctx.stmt(1)); //stmt
            newTexts.put(ctx, stamp() + a1 + " " + a2 + a3 + a4 + "\n" + stamp() + "{\n" + a5 + stamp() + "}\n" + stamp() + a6 + "\n" + stamp() + "{\n" + a7 + stamp() +"}\n"); //else문까지 있을때 같이 newTexts에 삽입
        }
    }

    @Override
    public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) { //return문 newTexts에 삽입
        if (ctx.getChildCount() == 2) { //그냥 return만있을때
            String a1 = ctx.getChild(0).getText(); //return
            String a2 = ctx.getChild(1).getText(); //;
            newTexts.put(ctx, stamp() + a1 + a2 + "\n");
        }
        else { //expr이있을때
            String a1 = ctx.getChild(0).getText(); //return
            String a2 = ctx.getChild(1).getText();
            String a3 = ctx.getChild(2).getText(); //;
            newTexts.put(ctx, stamp() + a1 + " " + a2 + a3 + "\n");
        }
    }

    @Override
    public void exitExpr(MiniCParser.ExprContext ctx) { //Expr
        if (isBinaryOperation(ctx)) { // expr 'operator' expr 인경우
            String a1 = newTexts.get(ctx.expr(0)); //expr
            String a2 = ctx.getChild(1).getText(); //operator
            String a3 = newTexts.get(ctx.expr(1)); //expr
            newTexts.put(ctx, a1 + " " + a2 + " " + a3); //붙여서 newTexts에 삽입
        }
        else if (ctx.getChildCount() == 1) { // IDENT거나 LITERAL인경우
            String a1 = ctx.getChild(0).getText();
            newTexts.put(ctx, a1);
        }
        else if (ctx.getChildCount() == 2 && ctx.getChild(0).getText().length() == 1) { //'- or +' expr 인경우
            String a1 = ctx.getChild(0).getText(); //- or +
            String a2 = newTexts.get(ctx.expr(0)); //expr
            newTexts.put(ctx, a1 + " " + a2);
        }
        else if (ctx.getChildCount() == 2 && ctx.getChild(0).getText().length() == 2) { //'-- or ++' expr
            String a1 = ctx.getChild(0).getText(); //-- or ++
            String a2 = newTexts.get(ctx.expr(0)); //expr
            newTexts.put(ctx, a1 + a2);
        }
        else if (ctx.getChildCount() == 3 && ctx.getChild(1) == ctx.expr()) { // '(' expr ')'인경우
            String a1 = ctx.getChild(0).getText();
            String a2 = newTexts.get(ctx.expr(0));
            String a3 = ctx.getChild(2).getText();
            newTexts.put(ctx, a1 + a2 + a3);
        }
        else if (ctx.getChildCount() == 4) {
            String a1 = ctx.IDENT().getText(); //IDENT
            String a2 = ctx.getChild(1).getText();//[ or (
            String a3;
            if(ctx.getChild(2) == ctx.expr(0)) { // IDENT '[' expr ']' 인경우
                a3 = newTexts.get(ctx.expr(0)); //expr
            }
            else { // IDENT '(' args ')' 인경우
                a3 = newTexts.get(ctx.args()); //args
            }
            String a4 = ctx.getChild(3).getText(); //] or )
            newTexts.put(ctx, a1 + a2 + a3 + a4);
        }
        else if(ctx.getChildCount() == 6) { // IDENT '[' expr ']' '=' expr 인경우
            String a1 = ctx.IDENT().getText(); //IDENT
            String a2 = ctx.getChild(1).getText(); //[
            String a3 = newTexts.get(ctx.expr(0)); //expr
            String a4 = ctx.getChild(3).getText(); //]
            String a5 = ctx.getChild(4).getText(); // =
            String a6 = newTexts.get(ctx.expr(1)); //expr
            newTexts.put(ctx, a1 + a2 + a3 + a4 + " " + a5 + " " + " " + a6);
        }
        else { // IDENT '=' expr 인경우
            String a1 = ctx.IDENT().getText(); //IDENT
            String a2 = ctx.getChild(1).getText(); //=
            String a3 = newTexts.get(ctx.expr(0)); //expr
            newTexts.put(ctx, a1 + " " + a2 + " " + a3);
        }
    }

    @Override
    public void exitArgs(MiniCParser.ArgsContext ctx) { //인자
        if (ctx.getChildCount() != 0) { //인자가 1개이상일떄
            String args = newTexts.get(ctx.expr(0)); //최소 1개는있으니 바로받고
            for(int i = 1; i < ctx.expr().size(); i++) { //2번째부터는 ,를붙여서 받는다.
                args = args + ", " + newTexts.get(ctx.expr(i));
            }
            newTexts.put(ctx, args);
        }
        else { //인자가 없을떄
            newTexts.put(ctx, "");
        }
    }

}
