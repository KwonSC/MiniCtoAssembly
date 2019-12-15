//201402318 권순창 컴파일러 HW05_3
package listener.main;

import java.util.Hashtable;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.*;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import generated.MiniCParser.ExprContext;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.ProgramContext;
import generated.MiniCParser.StmtContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;

import static listener.main.BytecodeGenListenerHelper.*;
import static listener.main.SymbolTable.*;

public class BytecodeGenListener extends MiniCBaseListener implements ParseTreeListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
	SymbolTable symbolTable = new SymbolTable();

	int tab = 0;
	int label = 0;

	// program	: decl+

	@Override
	public void enterFun_decl(Fun_declContext ctx) {
		symbolTable.initFunDecl();

		String fname = getFunName(ctx);
		ParamsContext params;
		symbolTable.putFunSpecStr(ctx);
		params = (ParamsContext) ctx.getChild(3);
		symbolTable.putParams(params);

	}


	// var_decl	: type_spec IDENT ';' | type_spec IDENT '=' LITERAL ';'|type_spec IDENT '[' LITERAL ']' ';'
	@Override
	public void enterVar_decl(Var_declContext ctx) {
		String varName = ctx.IDENT().getText();

		if (isArrayDecl(ctx)) {
			symbolTable.putGlobalVar(varName, Type.INTARRAY);
		}
		else if (isDeclWithInit(ctx)) {
			symbolTable.putGlobalVarWithInitVal(varName, Type.INT, initVal(ctx));
		}
		else  { // simple decl
			symbolTable.putGlobalVar(varName, Type.INT);
		}
	}


	@Override
	public void enterLocal_decl(Local_declContext ctx) {
		if (isArrayDecl(ctx)) {
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INTARRAY);
		}
		else if (isDeclWithInit(ctx)) {
			symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx));
		}
		else  { // simple decl
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INT);
		}
	}


	@Override
	public void exitProgram(ProgramContext ctx) {

		String fun_decl = "", var_decl = "";

		for(int i = 0; i < ctx.getChildCount(); i++) {
			if(isFunDecl(ctx, i))
				fun_decl += newTexts.get(ctx.decl(i));
			else
				var_decl += newTexts.get(ctx.decl(i));
		}

		newTexts.put(ctx,  var_decl + fun_decl);
		System.out.println(newTexts.get(ctx));
	}


	// decl	: var_decl | fun_decl
	@Override
	public void exitDecl(MiniCParser.DeclContext ctx) {
		String decl = "";
		if(ctx.getChildCount() == 1)
		{
			if(ctx.var_decl() != null)				//var_decl
				decl += newTexts.get(ctx.var_decl());
			else							//fun_decl
				decl += newTexts.get(ctx.fun_decl());
		}
		newTexts.put(ctx, decl);
	}

	// stmt	: expr_stmt | compound_stmt | if_stmt | while_stmt | return_stmt
	@Override
	public void exitStmt(StmtContext ctx) {
		String stmt = "";
		if(ctx.getChildCount() > 0)
		{
			if(ctx.expr_stmt() != null)	{
				stmt += newTexts.get(ctx.expr_stmt());// expr_stmt
			}
			else if(ctx.compound_stmt() != null)	// compound_stmt
				stmt += newTexts.get(ctx.compound_stmt());
			else if(ctx.while_stmt() != null)	// while_stmt
				stmt += newTexts.get(ctx.while_stmt());
			else if(ctx.if_stmt() != null)	// if_stmt
				stmt += newTexts.get(ctx.if_stmt());
			else if(ctx.return_stmt() != null)	// return_stmt
				stmt += newTexts.get(ctx.return_stmt());

			// <(0) Fill here>
		}
		newTexts.put(ctx, stmt);
	}

	// expr_stmt	: expr ';'
	@Override
	public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		String stmt = "";
		if(ctx.getChildCount() == 2)
		{
			stmt += newTexts.get(ctx.expr());	// expr
		}
		newTexts.put(ctx, stmt);
	}





	@Override
	public void exitFun_decl(Fun_declContext ctx) {
		String header = funcHeader(ctx, getFunName(ctx));
		header += newTexts.get(ctx.getChild(ctx.getChildCount()-1));
		header += "pop rbp\n";
		header += "ret\n";
		newTexts.put(ctx, header);
		// <(2) Fill here!>
	}


	private String funcHeader(Fun_declContext ctx, String fname) {
		return fname + ":\n"
				+ "push rbp\n"
				+ "mov rbp, rsp\n";

	}



	@Override
	public void exitVar_decl(Var_declContext ctx) {
		String varName = ctx.IDENT().getText();
		String varDecl = "";

		if (isDeclWithInit(ctx)) {
			varDecl += "putfield " + varName + "\n";
		}
		newTexts.put(ctx, varDecl);
	}


	@Override
	public void exitLocal_decl(Local_declContext ctx) {
		String varDecl = "";

		if (isDeclWithInit(ctx)) {
			String vId = symbolTable.getVarId(ctx);
			varDecl += "mov DWORD PTR [rbp-" + vId + "], " + ctx.LITERAL().getText()+ "\n";
		}

		newTexts.put(ctx, varDecl);
	}


	// compound_stmt	: '{' local_decl* stmt* '}'
	@Override
	public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		// <(3) Fill here>
		String compound = "";
		for(int i = 1; i < ctx.getChildCount() - 1; i++) {
			compound += newTexts.get(ctx.getChild(i));
		}
		newTexts.put(ctx, compound);
	}
	// while_stmt	: WHILE '(' expr ')' stmt
	@Override
	public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
		String stmt = "";
		String condExpr= newTexts.get(ctx.expr());
		String thenStmt = newTexts.get(ctx.stmt());
		String lend = symbolTable.newLabel();
		String lloop = symbolTable.newLabel();
		stmt += "jmp ." + lend + ":\n" +
				"." + lloop + ":\n" +
				thenStmt + lend + ":\n" +
				condExpr +"." + lloop + "\n";
		newTexts.put(ctx, stmt);
		// <(1) Fill here!>
	}

	// if_stmt	: IF '(' expr ')' stmt | IF '(' expr ')' stmt ELSE stmt;
	@Override
	public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
		String stmt = "";
		String condExpr= newTexts.get(ctx.expr());
		String thenStmt = newTexts.get(ctx.stmt(0));
		String lend = symbolTable.newLabel();
		String lelse = symbolTable.newLabel();


		if(noElse(ctx)) {
			stmt += condExpr + "." + lend + "\n"
					+ thenStmt + "\n"
					+ "." + lend + ":\n";
		}
		else {
			String elseStmt = newTexts.get(ctx.stmt(1));
			stmt += condExpr +  "." + lend + "\n"
					+ thenStmt + "\n"
					+ "." + lend + ":"  + "\n"
					+ elseStmt + "\n"
					+ "." + lelse + ": \n";

		}

		newTexts.put(ctx, stmt);
	}


	// return_stmt	: RETURN ';' | RETURN expr ';'
	@Override
	public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
		if(isIntReturn(ctx)) {
			String rt = "";
			String idName = ctx.getChild(1).getText();
			rt += newTexts.get(ctx.expr());
			rt += "mov eax, DWORD PTR [rbp-" + symbolTable.getVarId(idName) + "]\n";
			newTexts.put(ctx, rt);
		}
		// <(4) Fill here>
	}


	@Override
	public void exitExpr(ExprContext ctx) {
		String expr = "";

		if(ctx.getChildCount() <= 0) {
			newTexts.put(ctx, "");
			return;
		}

		if(ctx.getChildCount() == 1) { // IDENT | LITERAL
			if(ctx.IDENT() != null) {
				String idName = ctx.IDENT().getText();
				if(symbolTable.getVarType(idName) == Type.INT) {
					expr += "mov " + symbolTable.getRegister(idName)+", DWORD PTR [rbp-" + symbolTable.getVarId(idName) + "] \n";
				}
				//else	// Type int array => Later! skip now..
				//	expr += "           lda " + symbolTable.get(ctx.IDENT().getText()).value + " \n";
			} else if (ctx.LITERAL() != null) {
				String literalStr = ctx.LITERAL().getText();
				expr += "mov " +symbolTable.getRegister() + ", " + literalStr + " \n";
			}

		} else if(ctx.getChildCount() == 2) { // UnaryOperation
			expr = handleUnaryExpr(ctx,  expr);
		}
		else if(ctx.getChildCount() == 3) {
			if(ctx.getChild(0).getText().equals("(")) { 		// '(' expr ')'
				expr = newTexts.get(ctx.expr(0));

			} else if(ctx.getChild(1).getText().equals("=")) { 	// IDENT '=' expr
				String k = ctx.getChild(0).getText();
				String r0 = newTexts.get(ctx.expr(0));
				StringBuffer s = new StringBuffer(r0);
				s.reverse();
				r0 = s.toString();
				int i = 0;
				String x = "";
				boolean flag = false;
				while (true) {
					String a = r0.substring(i,i+1);
					if (a.equals(",") && !flag) {
						flag = true;
					}
					if (flag && a.equals(" ")) {
						break;
					}
					if (flag) {
						x += r0.substring(i+1, i+2);
					}
					i++;
				}
				s = new StringBuffer(x);
				r0 = s.reverse().toString().substring(1);
				expr += newTexts.get(ctx.expr(0));
				expr += "mov DWORD PTR [rbp-" + symbolTable.getVarId(k) + "], " + r0 + "\n";

			} else { 											// binary operation
				expr = handleBinExpr(ctx, expr);

			}
		}
		// IDENT '(' args ')' |  IDENT '[' expr ']'
		else if(ctx.getChildCount() == 4) {
			if(ctx.args() != null){		// function calls
				expr = handleFunCall(ctx, expr);
			} else { // expr
				// Arrays: TODO
			}
		}
		// IDENT '[' expr ']' '=' expr
		else { // Arrays: TODO			*/
		}
		newTexts.put(ctx, expr);
	}


	private String handleUnaryExpr(ExprContext ctx, String expr) {
		String l1 = symbolTable.newLabel();
		String l2 = symbolTable.newLabel();
		String lend = symbolTable.newLabel();

		expr += newTexts.get(ctx.expr(0));
		String r0 = newTexts.get(ctx.expr(0)).substring(4,7);
		switch(ctx.getChild(0).getText()) {
			case "-":
				expr += "neg DWORD PTR [rbp-" +symbolTable.getlocalVarid(r0) + "]\n";
				break;
			case "--":
				expr += "sub DWORD PTR [rbp-" +symbolTable.getlocalVarid(r0) + "], 1\n";
				break;
			case "++":
				expr += "add DWORD PTR [rbp-" +symbolTable.getlocalVarid(r0) + "], 1\n";
				break;
			case "!":
				expr += "ifeq " + l2 + "\n"
						+ l1 + ": " + "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;
		}
		return expr;
	}


	private String handleBinExpr(ExprContext ctx, String expr) {
		String r0 = newTexts.get(ctx.expr(0)).substring(4,7);
		String r1 = newTexts.get(ctx.expr(1)).substring(4,7);

		expr += newTexts.get(ctx.expr(0));
		expr += newTexts.get(ctx.expr(1));

		switch (ctx.getChild(1).getText()) {
			case "*":
				expr += "imul " + r0 + ", "+ r1 + "\n"; break;
			case "/":
				expr += "mov eax, DWORD PTR [rbp-" + symbolTable.getlocalVarid(r0) + "]\n"
						+  "cdq\n" + "idiv DWORD PTR[rbp-" + symbolTable.getlocalVarid(r1) + "]\n"; break;
			case "%":
				expr += "mov eax, DWORD PTR [rbp-" + symbolTable.getlocalVarid(r0) + "]\n"
						+  "cdq\n" + "idiv DWORD PTR[rbp-" + symbolTable.getlocalVarid(r1) + "]\n"
						+  "mov eax, edx\n"; break;
			case "+":// expr(0) expr(1) iadd
				expr += "add " + r0 + ", "+ r1 + "\n"; break;
			case "-":
				expr += "sub " + r0 + ", "+ r1 + "\n"; break;

			case "==":
				expr += "cmp "+ r0 +", "+ r1 +"\n"
						+ "jne ";
				break;
			case "!=":
				expr += "cmp "+ r0 +", "+ r1 +"\n"
						+ "je ";
				break;
			case "<=":
				expr += "cmp "+ r0 +", "+ r1 +"\n"
						+ "jg ";
				break;
			// <(5) Fill here>
			case "<":
				expr += "cmp "+ r0 +", "+ r1 +"\n"
						+ "jge ";
				break;
			// <(6) Fill here>

			case ">=":
				expr += "cmp "+ r0 +", "+ r1 +"\n"
						+ "jle ";
				break;
			// <(7) Fill here

			case ">":
				expr += "cmp "+ r0 +", "+ r1 +"\n"
						+ "jl ";
				break;
			// <(8) Fill here
			case "or":
				String l1 = symbolTable.newLabel();
				String l2 = symbolTable.newLabel();
				expr += "cmp "+ "DWORD PTR [rbp-" + symbolTable.getlocalVarid(r0) + "], " +"0\n"
						+ "je ." + l1 +"\n"+
						"cmp "+ "DWORD PTR [rbp-" + symbolTable.getlocalVarid(r0) + "], " +"0\n"
						+ "je ." + l2 +"\n";
				break;
			// <(9) Fill here>

			case "and":
				String l3 = symbolTable.newLabel();
				expr += "cmp "+ "DWORD PTR [rbp-" + symbolTable.getlocalVarid(r0) + "], " +"0\n"
						+ "je ." + l3 +"\n"+
						"cmp "+ "DWORD PTR [rbp-" + symbolTable.getlocalVarid(r0) + "], " +"0\n"
						+ "je ." + l3 +"\n";
				break;
		}
		return expr;
	}
	private String handleFunCall(ExprContext ctx, String expr) {
		String fname = getFunName(ctx);

		if (fname.equals("_print")) {
			expr =  newTexts.get(ctx.args()) + "cdqe\n"
					+ "mov eax, 0\n"
					+ "call printf\n";
		} else {
			expr = newTexts.get(ctx.args())
					+ "call " +  fname + "\n";
		}

		return expr;

	}

	// args	: expr (',' expr)* | ;
	@Override
	public void exitArgs(MiniCParser.ArgsContext ctx) {

		String argsStr = "";

		for (int i=0; i < ctx.expr().size() ; i++) {
			argsStr += newTexts.get(ctx.expr(i)) ;
		}
		newTexts.put(ctx, argsStr);
	}

}

