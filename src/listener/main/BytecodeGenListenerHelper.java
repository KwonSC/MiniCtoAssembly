//201402318 권순창 컴파일러 HW05_3
package listener.main;

import java.util.Hashtable;

import generated.MiniCParser;
import generated.MiniCParser.ExprContext;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.If_stmtContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;
import listener.main.SymbolTable;
import listener.main.SymbolTable.VarInfo;

public class BytecodeGenListenerHelper {
	
	// <boolean functions>
	
	static boolean isFunDecl(MiniCParser.ProgramContext ctx, int i) {
		return ctx.getChild(i).getChild(0) instanceof Fun_declContext;
	}
	
	// type_spec IDENT '[' ']'
	static boolean isArrayParamDecl(ParamContext param) {
		return param.getChildCount() == 4;
	}
	
	// global vars
	static int initVal(Var_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	// var_decl	: type_spec IDENT '=' LITERAL ';
	static boolean isDeclWithInit(Var_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}
	// var_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static boolean isArrayDecl(Var_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	// <local vars>
	// local_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static int initVal(Local_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	static boolean isArrayDecl(Local_declContext ctx) {
		return ctx.getChildCount() == 6;
	}
	
	static boolean isDeclWithInit(Local_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}
	
	static boolean isVoidF(Fun_declContext ctx) {
			// <Fill in>
		if (ctx.getChild(0).getText().equals("void")) {
			return true;
		}
		else {
			return false;
		}
	}
	static boolean isIntF(Fun_declContext ctx) {
		if (ctx.getChild(0).getText().equals("int")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	static boolean isIntReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() ==3;
	}


	static boolean isVoidReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() == 2;
	}
	
	// <information extraction>
	static String getStackSize(Fun_declContext ctx) {
		return "32";
	}
	static String getLocalVarSize(Fun_declContext ctx) {
		return "32";
	}
	static String getTypeText(Type_specContext typespec) {
			// <Fill in>
		String type = typespec.getText();
		if(type.compareTo("int") == 0) {
			return "I";
		}
		return "";
	}

	// params
	static String getParamName(ParamContext param) {
		return param.getChild(1).getText();
		// <Fill in>
	}
	
	static String getParamTypesText(ParamsContext params) {
		String typeText = "";
		
		for(int i = 0; i < params.param().size(); i++) {
			Type_specContext typespec = (Type_specContext)  params.param(i).getChild(0);
			typeText += getTypeText(typespec); // + ";";
		}
		return typeText;
	}

	static SymbolTable.Type getParamType(ParamContext param) {
		if(param.getChild(0).getText().equals("int")) {
			return SymbolTable.Type.INT;
		}
		else if (param.getChild(0).getText().equals("void")) {
			return SymbolTable.Type.VOID;
		}


		return null;
	}
	
	static String getLocalVarName(Local_declContext local_decl) {
		return local_decl.getChild(1).getText();
		// <Fill in>
	}
	
	static String getFunName(Fun_declContext ctx) {
		return ctx.getChild(1).getText();
		// <Fill in>
	}
	
	static String getFunName(ExprContext ctx) {
		return ctx.getChild(0).getText();
		// <Fill in>
	}
	
	static boolean noElse(If_stmtContext ctx) {
		return ctx.getChildCount() < 5;
	}
	
	static String getCurrentClassName() {
		return "Test";
	}
}
