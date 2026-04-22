import javax.swing.*;

public class TrainStation extends JFrame{

    //Database configiraton
    static final String db_url  = "url";
    static final String db_user = "root";
    static final String db_password = "password";
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } 
        catch (Exception ignored){}
    }
}