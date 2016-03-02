package nars.guifx.demo;

import nars.NAR;
import nars.guifx.NARide;
import nars.nar.Default;
import nars.task.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class NARfxDefaultIterative {

    static String recordfile = "C://myfile.txt";

    public static void main(String[] arg) {
        NARfxDefaultIterative def = new NARfxDefaultIterative();
        NAR nar = new Default(1000, 1, 1, 3);
       // nar.memory.eventInput.on(def::yourNewTaskTrackMethod);
        NARide.show(nar.loop(), (i) -> {});
    }

    public void yourNewTaskTrackMethod(Task t) {
        try {
            Files.write(Paths.get(recordfile), "".getBytes(), StandardOpenOption.CREATE_NEW); //create new if exists not already
        }catch(IOException ex) {}
        try {
            String wr = t.toString();
            if(t.isInput()) {
                wr = "IN: "+wr;
            } else {
                wr = "OUT: "+wr;
            }
            wr = wr + "\n";
            Files.write(Paths.get(recordfile), wr.getBytes(), StandardOpenOption.APPEND); //ok we can write into
        }catch (IOException e) {}
    }
}
