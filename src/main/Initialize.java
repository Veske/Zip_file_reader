package main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Initialize {
    static CentralDirReader central;
    static List<Byte> myBytesFin = new ArrayList<Byte>();
    static URL url = null;

    public static void main(String[] args) throws IOException, URISyntaxException {
        /*
         * We download the last 200 bytes of the file and see how big the Central directory is in that file
		 * After that we download the Central Directory, using the size provided by previous function
		 * We put the Central Directory into a byte array and then read out the content names from it
		 *
		 */

        if (args.length == 0) {
            System.out.println("No url has been provided, using the default URL...");
            url = new URL("http://apache.mirrors.tds.net/tomcat/tomcat-8/v8.0.15/bin/apache-tomcat-8.0.15.zip");
            //url = new URL("http://sourceforge.net/projects/xampp/files/XAMPP%20Windows/1.8.3/xampp-win32-1.8.3-4-VC11.zip/download");
        } else {
            url = new URL(args[0]);
        }

        central = new CentralDirReader(url);
        central.startTimer();
        central.CentralDirLength();
        byte[] bytes = central.CentralDirFull();
        central.clearMemory();

        PrintWriter pw = new PrintWriter(new FileWriter("output.txt"));
        int byteInt1, byteInt2, counter, totalLength = 0;
        int count = 0;
        byte lengthB1, lengthB2 = 0;

		/*
         * 	Main logic for reading the Central Folder from our small file
		 * 	We look for a magic number 0x02014b50, if we find it, we look for the length bytes that are after the signature
		 * 	Using the length bytes, we now know how long the name of the file is
		 * 	Now all that is left to do is to read out the name bytes, make a string out of them and print it out.
		 *
		 */


        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 80 && bytes[i + 1] == 75 && bytes[i + 2] == 01 && bytes[i + 3] == 02) {
                // If everything above passed the check, see how long the length byte 1 and 2 are, and then read so many bytes from the file name [byte]
                lengthB1 = bytes[i + 28];
                lengthB2 = bytes[i + 29];
                byteInt1 = lengthB1 & 0xff;
                byteInt2 = (lengthB2 & 0xff) * 256;
                totalLength = byteInt1 + byteInt2;
                counter = 0;
                for (int j = i + 46; j < i + 46 + totalLength; j++) {
                    if (j == bytes.length) {
                        break;
                    }
                    if (counter != totalLength) {
                        myBytesFin.add(bytes[j]);

                        counter++;
                    }
                }

                byte[] array = new byte[myBytesFin.size()];
                int x = 0;
                for (Byte p : myBytesFin) {
                    array[x] = p;
                    x++;
                }

                if (array.length > 1) {
                    String decoded = new String(array, "UTF-8");
                    pw.printf("%d %s \n", count, decoded);
                    System.out
                            .printf("%d %s \n", count, decoded);
                    count++;
                    myBytesFin.clear();


                }
            }
        }
        System.out.printf("\n\tTotal entrys: %d \t || \t Time elapsed: %d", count, central.getTime());
        central.stopTimer();
        pw.close();
    }
}
