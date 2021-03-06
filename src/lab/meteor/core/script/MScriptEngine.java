package lab.meteor.core.script;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class MScriptEngine {
	
	private ScriptEngineManager sem;
	
	public final ScriptEngine getScriptEngine() {
		ScriptEngine se = sem.getEngineByName("javascript");
		MScriptHelper helper = new MScriptHelper();
		se.getBindings(ScriptContext.ENGINE_SCOPE).put("me", helper);
		setBindings();
		return se;
	}
	
	protected void setBindings() { }
	
	public MScriptEngine() {
		sem = new ScriptEngineManager();
	}
	
}
