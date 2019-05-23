import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JFrame {
    JTextField textField1=new JTextField();
    JPasswordField textField2=new JPasswordField();

    LoginWindow(){
        GridLayout layout = new GridLayout(2,1);
        this.setLayout(layout);
        add(textField1);
        add(textField2);


        this.setSize(500,500);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

}
