package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.Timer;

import org.apache.commons.io.IOUtils;

public class CentralDirReader implements ActionListener {
    URL url = null;
    HttpURLConnection connection = null;
    RandomAccessFile file = null;
    FileInputStream fis = null;
    InputStream in = null;
    Timer t = new Timer(1000, this);
    static byte[] buffer = new byte[1024];
    byte[] bytes = null;
    int secondDL, time = 0;

    public CentralDirReader(URL url) {
        this.url = url;
    }

    public void CentralDirLength() throws IOException {

        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int firstDL = url.openConnection().getContentLength() - 200;
        connection.setRequestProperty("Range", "bytes=" + firstDL + "-");
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (connection.getResponseCode() / 100 != 2) {
            System.out.println("Response code is not in the 200 range!!");
            System.exit(0);
        }

        int contentLength = connection.getContentLength();
        if (contentLength < 1) {
            System.out.println("Something wrong with the file you are trying to see");
            System.exit(0);
        }

        file = new RandomAccessFile("cdCheck.zip", "rw");
        file.seek(firstDL);

        try {
            in = connection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            int read = in.read(buffer);
            if (read == -1) {
                break;
            }
            file.write(buffer, 0, read);
        }

        if (file != null) {
            try {
                file.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        in.close();

        try {
            fis = new FileInputStream("cdCheck.zip");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int one, two, three, four = 0;
        bytes = IOUtils.toByteArray(fis);
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 80 && bytes[i + 1] == 75 && bytes[i + 2] == 05 && bytes[i + 3] == 06) {
                one = bytes[i + 12] & 0xff;
                two = bytes[i + 13] & 0xff;
                three = bytes[i + 14] & 0xff;
                four = bytes[i + 15] & 0xff;
                secondDL = (four << 24) | (three << 16) | (two << 8) | (one << 2);
                break;
            }
        }
        file.close();
        file = null;
        File file = new File("cdCheck.zip");
        file.delete();
        bytes = null;
    }

    public byte[] CentralDirFull() throws IOException {

        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Length: " + url.openConnection().getContentLength() + " To fetch: " + secondDL);
        secondDL = url.openConnection().getContentLength() - secondDL;
        connection.setRequestProperty("Range", "bytes=" + secondDL + "-");

        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (connection.getResponseCode() / 100 != 2) {
            System.out.printf("Response code is not in the 200 range!!");
            System.exit(0);
        }

        int contentLength = connection.getContentLength();
        if (contentLength < 1) {
            System.out.printf("The file you are trying to access is not available or there is something wrong with it!");
            System.exit(0);
        }

        file = new RandomAccessFile("cdCheck.zip", "rw");
        file.seek(secondDL);

        try {
            in = connection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            int read = in.read(buffer);
            if (read == -1) {
                break;
            }
            file.write(buffer, 0, read);
        }

        if (file != null) {
            try {
                file.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        in.close();

        try {
            fis = new FileInputStream("cdCheck.zip");
        } catch (Exception e) {
            e.printStackTrace();
        }
        bytes = IOUtils.toByteArray(fis);
        file.close();
        file = null;
        File file = new File("cdCheck.zip");
        file.delete();

        return bytes;
    }

    public void startTimer() {
        t.start();
    }

    public void stopTimer() {
        t.stop();
    }

    public int getTime() {
        return time;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == t) {
            time++;
        }
    }

    public void clearMemory() {
        url = null;
        connection = null;
        file = null;
        fis = null;
        in = null;
        buffer = null;
        bytes = null;
    }
}
