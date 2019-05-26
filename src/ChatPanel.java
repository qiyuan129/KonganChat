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
    String username;
    String chattingFriend;
    StringBuffer chatRecord=new StringBuffer();

    ChatPanel(DatagramSocket socket1,String username1,String friend1){
        this.setLayout(new BorderLayout());
        socket=socket1;
        username=username1;
        chattingFriend=friend1;

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

        addSendListenr(enter);

//        addReceiveListener(display);

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
                            MyPacket myPacket=new MyPacket(3,username,chattingFriend,message+"\n");
                            byte[]data =MyPacket.toByte(myPacket);
                            DatagramPacket sendPacket=new DatagramPacket(data,data.length,InetAddress.getLocalHost(),5000);
                            socket.send(sendPacket);
                            System.out.println("给好友的第一个包已发送");
                            socket.send(sendPacket);
                            System.out.println("给好友的第二个包已发送");

                            //在自己的文本框中显示输入的信息
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                            display.append(username+" "+df.format(new Date())+"\n");
                            display.append(message+"\n");
                          text.setText("");
                        }
                        catch (UnknownHostException ex) {
                            //获取网络地址错误
                            ex.printStackTrace();
                        }
                        catch (IOException ex) {
                            //发送消息时出错
                            ex.printStackTrace();
                        }
                    }
                });
            }
        };
        Thread thread=new Thread(r);
        thread.start();
    }
    /** 接收并展示mainWindow传来的信息 */
    void showMessage(String message){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run() // updates displayArea
                    {

                        display.append(chattingFriend+" "+df.format(new Date())+"\n");// new Date()为获取当前系统时间
                        display.append(message+"\n");
//                        eventLog.append("  "+new String(receivePacket.getData(),0,receivePacket.getLength())+"\n");
                    } // end method run
                });
    }
    /** 单独开一个线程接收并展示收到的信息 */

    void displayMessage(String message){


    }

//    void addFriend(
//
//    )




}
