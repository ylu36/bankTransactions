package helper;
import java.io.*;

public class MyLogger {
    public static void success_log(String text) {
        BufferedWriter output = null;
        try {
            File file = new File("success.log");
            output = new BufferedWriter(new FileWriter(file, true));
            output.write(text);
            output.write('\n');
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
          if ( output != null ) {
                try {output.close();} catch (Exception ex) {/*ignore*/}
          }
        }
    }
    public static void fail_log(String text) {
        BufferedWriter output = null;
        try {
            File file = new File("failure.log");
            output = new BufferedWriter(new FileWriter(file, true));
            output.write(text);
            output.write('\n');
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
          if ( output != null ) {
                try {output.close();} catch (Exception ex) {/*ignore*/}
          }
        }
    }
}