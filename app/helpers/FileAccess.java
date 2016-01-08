package helpers;

import java.io.File;

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
}
