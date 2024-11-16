package com.github.ironbit;

import com.github.ironbit.files.CodeVertFile;
import com.github.ironbit.files.JsonFile;

public class Main {
    public static void main(String[] args) {
        //Creamos el objeto al ejecutar el evento de Drag and Drop
        CodeVertFile file = new JsonFile();

        FileConverter converter = new FileConverter(file, "csv");
    }
}