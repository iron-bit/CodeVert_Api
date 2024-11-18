package com.github.ironbit;

import com.github.ironbit.files.CodeVertFile;
import com.github.ironbit.files.JsonFile;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        //Creamos el objeto al ejecutar el evento de Drag and Drop
        File userFile = new File("/home/tymur/Documents/Api_Try/GUESTS.JSON");

        CodeVertFile file = new JsonFile(userFile);

        FileConverter converter = new FileConverter(file, "xml");
    }
}