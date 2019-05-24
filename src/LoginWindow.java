import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;

public class LoginWindow extends JFrame {
//    JPanel accountField=new JPanel();
//    JPanel passwordField=new JPanel();
    JTextField accountText=new JTextField();
    JPasswordField passwordText=new JPasswordField();
    JTextField accountHint=new JTextField(" 账号");
    JTextField passwordHint=new JTextField(" 密码");
    JButton enter=new JButton("登录");
    Font loginFont=new Font("微软雅黑",Font.PLAIN,18);
    DatagramSocket socket;

    LoginWindow(){
        this.setTitle("空安聊----登录");
        GridLayout layout = new GridLayout(5,1);
        this.setLayout(layout);

        //设置、添加账户区域
        accountHint.setEditable(false);
        accountHint.setBorder(null);
        accountHint.setFont(loginFont);
        accountText.setFont(loginFont);
        this.add(accountHint);
        this.add(accountText);

        //设置、添加密码输入区域
        passwordHint.setEditable(false);
        passwordHint.setBorder(null);
        passwordHint.setFont(loginFont);
        passwordText.setFont(loginFont);
        this.add(passwordHint);
        this.add(passwordText);

        //登录按钮的添加及事件的处理
        this.add(enter);
        enter.setBackground(new Color(226, 242, 249));
        enter.setFont(new Font("微软雅黑",Font.PLAIN,20));
        try {
            socket=new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println("创建socket的时候出现问题");
        }

        addSendThread(enter);
        addReceiveThread();

        this.setSize(300,200);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    void addSendThread(JButton button){
        Runnable r=new Runnable() {
            @Override
            public void run() {
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        MyPacket myPacket=new MyPacket(0,accountText.getText(),passwordText.getText());
                        byte data[]=MyPacket.toByte(myPacket);
                        DatagramPacket sendPacket= null;
                        try {
                            sendPacket = new DatagramPacket(data,data.length, InetAddress.getLocalHost(),5000);
                        } catch (UnknownHostException ex) {
                            ex.printStackTrace();
                        }

                        try {
                            socket.send(sendPacket);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            System.out.println("发送数据包时出现问题");
                        }
                    }
                });
            }
        };

        Thread thread=new Thread(r);
        thread.start();

    }
    void addReceiveThread(){
        Runnable r=new Runnable() {
            @Override
            public void run() {
                while(true){
                    byte[] data=new byte[200];
                    DatagramPacket receivePacket=new DatagramPacket(data,data.length);

                    try {
                        socket.receive(receivePacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("接收数据包时出现问题");
                    }

                    MyPacket myPacket=MyPacket.decodeByte(receivePacket.getData());
                    int status=myPacket.getStatus();
                    if(status==1){
                        JOptionPane.showMessageDialog(null,"登录成功！");
                        dispose();
                        MainWindow window = new MainWindow();
                    }
                    else if(status==2){
                        JOptionPane.showMessageDialog(null,"登陆失败！");
                    }
                    else{

                    }

                }
            }
        };
        Thread thread=new Thread(r);
        thread.start();
    }
}
