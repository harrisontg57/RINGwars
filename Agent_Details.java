import java.awt.*;

public class Agent_Details {
    public String filename;
    public String locname;
    public String lang;
    public Color color;
    public int myStart; //The global location which the agent considers 0.
    public Agent_Details(String filename, String locname, String lang, Color color) {
        this.filename = filename;
        this.locname = locname;
        this.lang = lang;
        this.color = color;
    }
    public String getFileName() {
        return this.filename;
    }
    public String getlocName() {
        return this.locname;
    }
    public String getLang() {
        return this.lang;
    }
    public Color getColor() {
        return this.color;
    }
}