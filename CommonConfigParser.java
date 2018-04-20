import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class CommonConfigParser {

    public int NumberOfPreferredNeighbors;
    public int UnchokingInterval;
    public int OptimisticUnchokingInterval;
    public String Filename;
    public int filesize;
    public int piecesize;

    public void saveCommonConfig() {

        BufferedReader commoncfgreader;
        try {
            commoncfgreader = new BufferedReader(new FileReader("Common.cfg"));

            String nextline = null;
            try {
                while ((nextline = commoncfgreader.readLine()) != null) {

                    String[] commonConfigTokens = nextline.split("\\s+");

                    if (nextline.contains("NumberOfPreferredNeighbors")) {

                        NumberOfPreferredNeighbors = Integer.parseInt(commonConfigTokens[1]);

                    }

                    if (nextline.contains("UnchokingInterval")) {

                        UnchokingInterval = Integer.parseInt(commonConfigTokens[1]);

                    }
                    if (nextline.contains("OptimisticUnchokingInterval")) {

                        OptimisticUnchokingInterval = Integer.parseInt(commonConfigTokens[1]);


                    }
                    if (nextline.contains("FileName")) {

                        Filename = commonConfigTokens[1];

                    }
                    if (nextline.contains("FileSize")) {

                        filesize = Integer.parseInt(commonConfigTokens[1]);

                    }
                    if (nextline.contains("PieceSize")) {

                        piecesize = Integer.parseInt(commonConfigTokens[1]);

                    }


                }
            }
            catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
