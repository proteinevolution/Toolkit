package helpers;

import java.io.*;

/**
 * Handles the directory structures that need to be created due to Job Execution
 *
 * Created by lukas on 1/8/16.
 */
public class FileAccess {


    /**
     * Avoids instantiation of File Access
     */
    private FileAccess() {
    }


    public static boolean mkdir(String name) {

        return new File(name).mkdirs();
    }

    public static boolean mkfile(String name, String content) {


        BufferedWriter bf;

        try {
            bf = new BufferedWriter(new FileWriter(new File(name)));
            bf.write(content);
            bf.close();
            return true;
        }
        catch(IOException e) {

            e.printStackTrace();
            return false;
        }
    }
}
