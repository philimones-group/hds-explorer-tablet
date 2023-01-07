package org.philimone.hds.explorer.server.settings.generator;

public class CodeGeneratorFactory {
    public static CodeGenerator newInstance(){
        return new DefaultCodeGenerator(); //to use a different code patterns, implements CodeGenerator and instatiate the class here
    }
}