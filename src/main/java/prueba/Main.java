package prueba;

import com.github.ironbit.CodeVertFile;
import com.github.ironbit.FileConverter;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        //Creamos el objeto al ejecutar el evento de Drag and Drop
        File userFile = new File("/home/tymur/Documents/Api_Try/GUESTS.JSON");

        FileConverter converter = new FileConverter();

        CodeVertFile file = converter.prepareFile(userFile);
        converter.convert(file, "XML");
    }
}