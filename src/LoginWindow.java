import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.util.Random;

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
    int currentPort=-1;
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

        /** 用随机指定的端口进行连接，直到成功为止 */
//        while(true) {
//            try {
//                Random random = new Random();
        try {
            socket = new DatagramSocket();
            currentPort=socket.getLocalPort();
            System.out.println("socket创建成功,绑定端口号为"+socket.getLocalPort()+",currentPort="+currentPort);
        } catch (SocketException e) {
            System.out.println("创建socket失败");
            e.printStackTrace();
        }
//
//                //socket定义成功则记录下使用的端口号
//                System.out.println("socket申请成功");
//                currentPort=socket.getPort();
//                break;
//            } catch (SocketException e) {
//                e.printStackTrace();
//                System.out.println("端口号已被占用，准备测试下一个随机端口号");
//                continue;
//            }
//        }
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
                    byte[] data=new byte[512];
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
                        MainWindow window = new MainWindow(socket,myPacket.getFriends(),accountText.getText());
                        System.out.println(myPacket.getFriends());
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
