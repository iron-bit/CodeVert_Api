package prueba;

import com.github.ironbit.CodeVertFile;
import com.github.ironbit.FileConverter;
import com.github.ironbit.FileExtension;

import java.io.File;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        //Creamos el objeto al ejecutar el evento de Drag and Drop
        File userFile = new File("/home/tymur/Documents/CodeVertApi/src/main/resources/smth.json");
        FileConverter fileConverter = new FileConverter();
        CodeVertFile file = fileConverter.prepareFile(userFile);
        Set<FileExtension> compatibleExtensions = fileConverter.getCompatibleExtensions(file);
        compatibleExtensions.forEach(System.out::println);
        fileConverter.convert(file, FileExtension.CSV, "menuitem");
    }
}