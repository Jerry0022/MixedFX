import de.mixedfx.java.ComplexString;
import de.mixedfx.windows.ahk.AHKManager;

import java.io.IOException;

/**
 * Created by India_000 on 28.09.2015.
 */
public class AHKTester {
    public static void main(String [] args) throws IOException {
        AHKManager.runAHKCommands("MsgBox, 1a", true);
        AHKManager.runAHKCommands("MsgBox, 1b", true);

        ComplexString str = new ComplexString();
        str.add("MsgBox, 2");
        str.add("MsgBox, 3");
        AHKManager.runAHKCommands(str, true);

        AHKManager.runEmbeddedAHKFile(AHKTester.class, "Test.ahk", true);
    }
}
