import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileSpool {
    private StringBuilder sb;
    private String name, outFolder;
    private int lines = 0;
    public FileSpool(String name, String outFolder){
        this.name = name;
        this.sb = new StringBuilder();
        this.outFolder = outFolder;
    }
    public FileSpool(String name, String outFolder, String header){
        this.name = name;
        this.sb = new StringBuilder();
        addLine(header);
        this.outFolder = outFolder;
    }

    public void addLine(String text){
        sb.append(text + "\n");
        lines += 1;
    }

    public int getLines(){
        return lines;
    }
    public void addString(String text){
        sb.append(text);
    }

    // Returns true if successfully writes
    public boolean write(){
        String dirName = outFolder;
        String filePath = dirName + File.separator + name;
        if(!name.substring(name.length()-4).equals(".csv)")){
             filePath += ".csv";
        }
        File directory = new File(dirName);
        if (! directory.exists()){
            directory.mkdirs();
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            bw.write(sb.toString());
            bw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
