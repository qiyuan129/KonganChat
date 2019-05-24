import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

public class Server extends JFrame {
    private DatagramSocket socket;
//    private JTextField broadcast;
    private JTextArea eventLog;
    HashMap<String,AccountInformation> accounts=new HashMap<>();
    Server(){
        super("空安聊---服务器");
        this.setLayout(new BorderLayout());

        eventLog=new JTextArea();
        eventLog.setFont(new Font("黑体",Font.PLAIN,18));
        add( new JScrollPane( eventLog), BorderLayout.CENTER );

        try {
            socket=new DatagramSocket(5000);
        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println("创建服务器socket时出现问题");
            System.exit(1);
        }

        this.addAccount("221701225","666");

        this.setSize(700,700);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    void waitForMessage(){
        while(true){
            try {
                byte data1[]=new byte[256];
                DatagramPacket receivePacket=new DatagramPacket(data1,data1.length);
                //接收信息
                socket.receive(receivePacket);
                //在服务器端的eventlog中显示接收到的信息
//                displayMessage(receivePacket);
                ServerThread serverThread=new ServerThread(receivePacket);
                serverThread.start();


//                byte data2[]=str.getBytes();
//
//                DatagramPacket responsePacket=new DatagramPacket(data2,data2.length,receivePacket.getAddress(),receivePacket.getPort());
//                socket.send(responsePacket);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打印收到信息的时间、来源端口号以及自定义信息
     * @param receivePacket  接收到的数据报Packet
     * @param message  一条自定义的字符串信息
     */
    void displayMessage(DatagramPacket receivePacket,String message){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run() // updates displayArea
                    {

                        eventLog.append(df.format(new Date())+"     FROM port："+receivePacket.getPort()+"\n");// new Date()为获取当前系统时间
                        eventLog.append(message+"\n");
//                        eventLog.append("  "+new String(receivePacket.getData(),0,receivePacket.getLength())+"\n");
                    } // end method run
                } // end anonymous inner class
        ); //

//        repaint();
//        System.out.println(df.format(new Date())+"     FROM port："+receivePacket.getPort());// new Date()为获取当前系统时间
//
//        System.out.println(("  "+new String(receivePacket.getData(),0,receivePacket.getData().length)));
    }

    /**
     * 给定账号和密码 ，在服务器的账号列表里添加一个对象*
     * @param account1  待添加账户的账号
     * @param password1 待添加账户的密码
     */
    void addAccount(String account1,String password1){
        AccountInformation accountInfo=new AccountInformation(password1);
        accounts.put(account1,accountInfo);
    }

    /**
     * 处理某个账号发起的添加好友请求
     * @param account1 发起添加好友请求的账号
     * @param account2 等待被添加的人的账号
     */
    void addFriend(String account1,String account2){

    }

    /** 处理客户端发起的登录请求
     * @param packet 带有登录请求信息的数据包
     * @return  登录信息验证成功返回true，验证失败返回false
     */
    boolean processLoginRequest(MyPacket packet){
        AccountInformation accountInfo=accounts.get(packet.getAccount());    //尝试从服务器的Hashmap中获得账户信息
        if(accountInfo==null){
            System.out.println("登陆失败，服务器中找不到该账号");
            return false;
        }
        else{
            if(packet.getPassword().equals(accountInfo.getPassword())==false){   //密码匹配失败
                System.out.println("登陆失败，密码错误");
                return false;
            }
            else{
                System.out.println("登陆成功，准备回发好友信息");
                return true;
            }
        }
    }

    /** 服务器线程类 一个包专门由一个线程来处理 */
    class ServerThread extends  Thread{
        DatagramPacket receivePacket;
        ServerThread(DatagramPacket datagramPacket1){
            receivePacket=datagramPacket1;
        }

        @Override
        public void run() {
            MyPacket receiveData=MyPacket.decodeByte(receivePacket.getData());
            int type=receiveData.getStatus();
            if(type==0){
                displayMessage(receivePacket,receiveData.getAccount()+"请求登录");

                boolean loginState;
                loginState=processLoginRequest(receiveData);

                if(loginState==false){
                    //构造返回的datagramPacket对象
                    MyPacket myPacket=new MyPacket(2);
                    byte[] data2=MyPacket.toByte(myPacket);
                    DatagramPacket responsePacket=new DatagramPacket(data2,data2.length,receivePacket.getAddress(),
                            receivePacket.getPort());
                    //发送数据报
                    try {
                        socket.send(responsePacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("服务器发送数据报时出现问题");
                    }
                }
                else{
                    AccountInformation accountInfo=accounts.get(receiveData.getAccount());
                    //把服务器存有好友信息的vector转为Friends数组后，存在数据报中返回
                    int friendsNumber=accountInfo.friends.size();
                    System.out.println("这个账号好友数量为"+friendsNumber+"\n");
                    MyPacket myPacket=new MyPacket(1,accountInfo.friends.toArray(new Friend[friendsNumber]));
                    byte[] data2=MyPacket.toByte(myPacket);
                    DatagramPacket responsePacket=new DatagramPacket(data2,data2.length,receivePacket.getAddress(),
                            receivePacket.getPort());
                    //发送数据包
                    try {
                        socket.send(responsePacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("服务器发送数据报时出现问题");
                    }
                }
            }
            else{
                System.out.println("未处理的数据包类型");
            }

        }
    }
}

/** 存有某个账号的密码，以及好友的名称和端口号 */
class AccountInformation{
    String password;
    Vector<Friend> friends;
    AccountInformation(String password1){
        password=password1;
        friends=new Vector<>();
    }
    void addFriend(Friend person){
        friends.add(person);
    }
    String getPassword(){
        return password;
    }

}


