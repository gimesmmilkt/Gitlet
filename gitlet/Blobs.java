package gitlet;
import java.io.File;
import java.io.Serializable;
public class Blobs implements Serializable {
    private File f;
    private byte[] bytecontent;
    private String filename;
    private String sha1code;
    private String content;

    /**
     * constructor of a blob
     * @param f File given by users
     * @param filename Name of the file
     */
        public Blobs(File f, String filename) {
        this.filename = filename;
        this.bytecontent = Utils.readContents(f);
        this.sha1code = Utils.sha1(bytecontent);
        this.content = Utils.readContentsAsString(f);
        this.f = f;
    }
    public String getsha1() {
        return sha1code;
    }
    public String getcontent() {
        return content;
    }

    /**
     * replace the Content of a blob
     * @param s new content as string
     */
    public void overwriteContent(String s) {
        Utils.writeContents(this.f, s);
    }
}



