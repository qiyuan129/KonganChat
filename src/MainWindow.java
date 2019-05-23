import javax.swing.*;

public class MainWindow extends JFrame{
    JTabbedPane friend=new JTabbedPane(SwingConstants.LEFT);

    MainWindow(){
        this.setTitle("空安聊----主界面");
        this.add(friend);

        ChatPanel chatWithServer=new ChatPanel();
        friend.addTab("练习时长两年半的管理员鸭鸭",chatWithServer);

        this.setSize(700,700);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }
}
