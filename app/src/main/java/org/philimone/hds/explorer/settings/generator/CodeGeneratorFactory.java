package org.philimone.hds.explorer.settings.generator;

public class CodeGeneratorFactory {
    static CodeGenerator newInstance(){
        return new DefaultCodeGenerator(); //to use a different code patterns, implements CodeGenerator and instatiate the class here
    }
}
