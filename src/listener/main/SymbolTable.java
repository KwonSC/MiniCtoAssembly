//201402318 권순창 컴파일러 HW05_2
package listener.main;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import generated.MiniCParser;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;
import listener.main.SymbolTable.Type;
import static listener.main.BytecodeGenListenerHelper.*;


public class SymbolTable {
	enum Type {
		INT, INTARRAY, VOID, ERROR
	}
	
	static public class VarInfo {
		Type type; 
		int id;
		int initVal;
		
		public VarInfo(Type type,  int id, int initVal) {
			this.type = type;
			this.id = id;
			this.initVal = initVal;
		}
		public VarInfo(Type type,  int id) {
			this.type = type;
			this.id = id;
			this.initVal = 0;
		}
	}
	
	static public class FInfo {
		public String sigStr;
	}
	
	private Map<String, VarInfo> _lsymtable = new HashMap<>();	// local v.
	private Map<String, VarInfo> _gsymtable = new HashMap<>();	// global v.
	private Map<String, FInfo> _fsymtable = new HashMap<>();	// function

	private String[] register = {"ebx", "ecx", "edx", "esi", "edi", "esp", "ebp"};

	private int _globalVarID = 0;
	private int _localVarID = 0;
	private int _paramVarID = 0;
	private int _labelID = 0;
	private int _tempVarID = 0;
	
	SymbolTable(){
		initFunDecl();
		initFunTable();
	}
	
	void initFunDecl(){		// at each func decl
		_lsymtable.clear();
		_localVarID = 0;
		_paramVarID = 16;
		_labelID = 0;
		_tempVarID = 32;		
	}
	
	void putLocalVar(String varname, Type type){
		this._lsymtable.put(varname, new VarInfo(type, _localVarID+=4));
		//<Fill here>
	}
	
	void putGlobalVar(String varname, Type type){
		this._gsymtable.put(varname, new VarInfo(type, _globalVarID+=4));
		//<Fill here>
	}
	
	void putLocalVarWithInitVal(String varname, Type type, int initVar){
		this._lsymtable.put(varname, new VarInfo(type, _localVarID+=4, initVar));
		//<Fill here>
	}
	void putGlobalVarWithInitVal(String varname, Type type, int initVar){
		this._gsymtable.put(varname, new VarInfo(type, _globalVarID+=4, initVar));
		//<Fill here>
	
	}
	
	void putParams(ParamsContext params) {
		for(int i = 0; i < params.param().size(); i++) {
			String varname = getParamName(params.param(i));
			Type type = getParamType(params.param(i));
			this._lsymtable.put(varname, new VarInfo(type, _paramVarID +=4));
		//<Fill here>
		}
	}
	
	private void initFunTable() {
		FInfo printlninfo = new FInfo();
		printlninfo.sigStr = "java/io/PrintStream/println(I)V";
		FInfo maininfo = new FInfo();
		maininfo.sigStr = "main([Ljava/lang/String;)V";
		_fsymtable.put("_print", printlninfo);
		_fsymtable.put("main", maininfo);
	}
	
	public String getFunSpecStr(String fname) {		
		// <Fill here>
		if (fname.equals("printf")) {
			return _fsymtable.get("_print").sigStr;
		}
		return _fsymtable.get(fname).sigStr;
	}

	public String getFunSpecStr(Fun_declContext ctx) {
		// <Fill here>
		return getFunSpecStr(ctx.getChild(1).getText());
	}
	
	public String putFunSpecStr(Fun_declContext ctx) {
		String fname = getFunName(ctx);
		String argtype = "";	
		String rtype = "";
		String res = "";
		
		// <Fill here>
		if(isIntF(ctx))
			rtype +="I";
		else if(isVoidF(ctx))
			rtype +="V";

		ParamsContext params = (MiniCParser.ParamsContext) ctx.getChild(3);
		argtype = getParamTypesText(params);
		res =  fname + "(" + argtype + ")" + rtype;
		
		FInfo finfo = new FInfo();
		finfo.sigStr = res;
		_fsymtable.put(fname, finfo);

		return res;
	}
	
	String getVarId(String name){
		// <Fill here>
		if(_lsymtable.containsKey(name)) {
			return Integer.toString(_lsymtable.get(name).id);
		}
		else if(_gsymtable.containsKey(name)) {
			return Integer.toString(_gsymtable.get(name).id);
		}
		return "";
	}

	String getRegister(String name){
		// <Fill here>
		if(_lsymtable.containsKey(name)) {
			return register[(_lsymtable.get(name).id - 1) / 4];
		}
		else if(_gsymtable.containsKey(name)) {
			return register[(_gsymtable.get(name).id - 1) / 4];
		}
		return "";
	}
	String getRegister(){
		// <Fill here>
		return register[_paramVarID/=4];
	}
	
	Type getVarType(String name){
		VarInfo lvar = (VarInfo) _lsymtable.get(name);
		if (lvar != null) {
			return lvar.type;
		}
		
		VarInfo gvar = (VarInfo) _gsymtable.get(name);
		if (gvar != null) {
			return gvar.type;
		}
		
		return Type.ERROR;	
	}
	String newLabel() {
		return "label" + _labelID++;
	}
	
	String newTempVar() {
		String id = "";
		return id + _tempVarID--;
	}

	// global
	public String getVarId(Var_declContext ctx) {
		// <Fill here>
		String sname = "";
		sname += getVarId(ctx.IDENT().getText());
		return sname;
	}

	// local
	public String getVarId(Local_declContext ctx) {
		String sname = "";
		sname += getVarId(ctx.IDENT().getText());
		return sname;
	}

	public int get_localVarID() {
		return _localVarID;
	}

	public int getlocalVarid(String id) {
		for (int i = 0; i < 7; i++) {
			if (id.equals(register[i])) {
				return (i + 1) * 4;
			}
		}
		return 0;
	}
	
}
