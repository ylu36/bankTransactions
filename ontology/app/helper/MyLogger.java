package helper;
import java.io.*;
// import org.drools.spi.KnowledgeHelper;

public class MyLogger {
    public static void log(String text) {
        BufferedWriter output = null;
        try {
            File file = new File("aa.log");
            output = new BufferedWriter(new FileWriter(file, true));
            output.write(text );//+ " with " + drools.getRule().getName());
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
          if ( output != null ) {
                try {output.close();} catch (Exception ex) {/*ignore*/}
          }
        }
    }
}