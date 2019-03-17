package com.kunfei.bookshelf.constant;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class EngineHelper {
    public static final ScriptEngine INSTANCE = new ScriptEngineManager().getEngineByName("rhino");
}
