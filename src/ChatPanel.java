import javax.imageio.plugins.tiff.TIFFImageReadParam;
import javax.print.attribute.standard.RequestingUserName;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ChatPanel extends JPanel {
//    Container ct;
//    BackgroundPanel backgroundPanel;
    JPanel textField=new JPanel();
    JTextArea text=new JTextArea("文本框");
    JButton enter=new JButton("发送");
    JTextArea display=new JTextArea("聊天记录\n");
//    JPanel friend=new JPanel();
    DatagramSocket socket;
    StringBuffer chatRecord=new StringBuffer();

    ChatPanel(){
        this.setLayout(new BorderLayout());


        //添加组件(及设置）之----输入框，回车按钮（都放在一个名为Textfield的JPane中
        Font font1=new Font("黑体",Font.PLAIN,28);
        textField.setLayout(new BorderLayout());
        textField.add(text,BorderLayout.CENTER);
        textField.add(enter,BorderLayout.EAST);
        text.setFont(font1);
        text.setBackground(new Color(228, 249, 249));
        enter.setFont(font1);
        enter.setBackground(new Color(225, 247, 255));
        text.setSize(600,180);
        enter.setSize(100,180);
        this.add(textField,BorderLayout.SOUTH);

        //添加组件（及设置）之----聊天记录显示部分（名为display的JTextArea
        this.add(new JScrollPane(display),BorderLayout.CENTER);
        display.setFont(new Font("黑体",Font.PLAIN,18));
        display.setBackground(new Color(239, 248, 243));

        //添加组件（及设置）之----好友列表JList
//        friend.setFont(new Font("宋体",Font.BOLD,16));
//        friend.setBackground(new Color(210, 241, 248));
//        this.add(new JScrollPane(friend),BorderLayout.EAST);
//        friend.setLayout(new GridLayout(15,1));
//        friend.setPreferredSize(new Dimension(180, 525));
//        friend.add(new JButton("叶尤澎"));
//
//        friend.add(new JButton("叶尤澎"));
//        friend.add(new JButton("叶尤澎"));
//        friend.add(new JButton("叶尤澎"));
//        friend.add(new JButton("叶尤澎"));
//        friend.add(new JButton("叶尤澎"));
//        friend.add(new JButton("叶尤澎"));
//        friend.add(new JButton("叶尤澎"));
//        friend.add(new JButton("叶尤澎"));
//        friend.add(new JButton("叶尤澎"));
//        friend.add(new JButton("叶尤澎"));

//        JButton friend1=new JButton("工作两年半的偶像管理员鸭鸭");
//        friend1.setSize(70,35);
//        friend1.setFont(new Font("微软雅黑",Font.PLAIN,13));
//        friend1.setBackground(new Color(219, 241, 249));
//        friend1.setMargin(new Insets(0,0,0,15));
//        friend.add(friend1);
//        Random r=new Random();
        //创建发送、接收数据的socket
        try {
            socket=new DatagramSocket(5001);
        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println("创建socket时出现问题");
        }
        addSendListenr(enter);
        addReceiveListener(display);

        this.setSize(700,700);
        this.setVisible(true);
    }

    /** 单独用一个线程来对信息的发送做监听 */
    void addSendListenr(JButton button){
        Runnable r=new Runnable() {
            @Override
            public void run() {
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            //把输入的数据发送给服务器
                            String message=text.getText();
                            byte[]data =message.getBytes();
                            DatagramPacket sendPacket=new DatagramPacket(data,data.length,InetAddress.getLocalHost(),5000);
                            socket.send(sendPacket);

                            //在自己的文本框中显示输入的数据
                            display.append("        "+message);
                            Thread.sleep(50);
                        }
                        catch (UnknownHostException ex) {
                            //获取网络地址错误
                            ex.printStackTrace();
                        }
                        catch (IOException ex) {
                            //发送消息时出错
                            ex.printStackTrace();
                        } catch (InterruptedException ex) {

                            ex.printStackTrace();
                        }
                    }
                });
            }
        };
        Thread thread=new Thread(r);
        thread.start();
    }
    /** 单独开一个线程接收并展示收到的信息 */
    void addReceiveListener(JTextArea display){
        Runnable r= new Runnable() {
            @Override
            public void run() {
                while(true){
                    byte[] data=new byte[100];
                    DatagramPacket receivePacket=new DatagramPacket(data,data.length);

                    //接收数据包
                    try {
                        socket.receive(receivePacket);
                    } catch (IOException e) {
                        System.out.println("接收数据包时出现问题");
                        e.printStackTrace();
                    }

                    //在display组件上添加接收到的信息
                    String newMessage=new String(receivePacket.getData(),0,receivePacket.getLength());
                    display.append(newMessage+"\n");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //为了方便调试，在控制台输出内容
                    displayMessage(receivePacket);
                }
            }
        };
        Thread thread=new Thread(r);
        thread.start();
    }
    void displayMessage(DatagramPacket receivePacket){

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date())+"     FROM port："+receivePacket.getPort());// new Date()为获取当前系统时间
        System.out.println(("  "+new String(receivePacket.getData(),0,receivePacket.getLength())));
    }

//    void addFriend(
//
//    )




}
