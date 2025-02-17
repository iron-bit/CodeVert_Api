package prueba;

import com.github.ironbit.CodeVertFile;
import com.github.ironbit.FileConverter;
import com.github.ironbit.FileExtension;

import java.io.File;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        //Creamos el objeto al ejecutar el evento de Drag and Drop
        File userFile = new File("/home/tymur/Desktop/streams2.json");
        FileConverter fileConverter = new FileConverter();
        CodeVertFile file = fileConverter.prepareFile(userFile);
        file.getKeys().forEach(System.out::println);
        Set<FileExtension> compatibleExtensions = fileConverter.getCompatibleExtensions(file);
        compatibleExtensions.forEach(System.out::println);
        String result = fileConverter.convert(file, FileExtension.CSV, null);
    }
}