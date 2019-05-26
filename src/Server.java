import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.*;
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

        this.addAccount("元","666");
        addFriend("元","唱跳rap");
        addFriend("元","鸭鸭加油");
        this.addAccount("鸭鸭加油","asp");
        addFriend("鸭鸭加油","元");
        this.addAccount("唱跳rap","cxk");
        addFriend("唱跳rap","元");

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
        accountInfo.setCurrentPort(-1);
        accounts.put(account1,accountInfo);
    }

    /**
     * 处理某个账号发起的添加好友请求
     * @param account1 发起添加好友请求的账号
     * @param account2 等待被添加的人的账号
     */
    void addFriend(String account1,String account2){
        AccountInformation accountInfo=accounts.get(account1);
        if(accountInfo==null){
            System.out.println("找不到账号1(发起添加好友请求的账号");
        }
        else{
            accountInfo.addFriend(account2);
        }
    }

    /** 处理客户端发起的登录请求
     * @param packet 带有登录请求信息的数据包
     * @return  登录信息验证成功返回true，验证失败返回false
     */
    boolean getLoginStatus(MyPacket packet, InetAddress address,int port){
        AccountInformation accountInfo=accounts.get(packet.getAccount());    //尝试从服务器的 Hashmap中获得账户信息
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
            int type=receiveData.getStatus();     //获取数据报类型

            /** 数据报类型：登录请求 */
            if(type==0){
                displayMessage(receivePacket,receiveData.getAccount()+"请求登录");

                boolean loginState;
                loginState=getLoginStatus(receiveData,receivePacket.getAddress(),receivePacket.getPort());

                /**登录失败*/
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
                /** 登录成功*/
                else{
                    AccountInformation accountInfo=accounts.get(receiveData.getAccount());

                    //记录该账号登录所用的端口号，为以后转发信息做准备
                    accountInfo.setCurrentPort(receivePacket.getPort());
                    //把服务器存有好友信息的vector转为Friends数组后，存在数据报中返回
                    int friendsNumber=accountInfo.friends.size();
                    System.out.println("这个账号好友数量为"+friendsNumber+"\n");

                    /***以下调试用*/

//                    System.out.println("这个账号的朋友有：");
//                    for(String str:friends){
//                        System.out.println(str);
//                    }
                    /** 以上调试用 */
                    String []friends=accountInfo.friends.toArray(new String[friendsNumber]);
                    MyPacket myPacket=new MyPacket(1,friends);
//
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
            /** 数据报类型：向好友发送的信息 */
            else if(type==3){
                String destination=receiveData.getDestination();    //信息目的地
                String origin=receiveData.getOrigin();              //信息源头
                displayMessage(receivePacket,origin+"向"+destination+"发送了信息");

                AccountInformation accountInfo = accounts.get(destination);
                if(accountInfo==null){
                    displayMessage(receivePacket,"在服务器中找不到接收对象");
                }
                /**接收对象当前不在线 */
                else if(accountInfo.getCurrentPort()==-1){
                    displayMessage(receivePacket,"接受对象当前不在线，发送失败");
                    MyPacket myPacket=new MyPacket(5);
                    byte[] data2=MyPacket.toByte(myPacket);

                    try {
                        DatagramPacket sendPacket = new DatagramPacket(data2,data2.length, InetAddress.getLocalHost(),
                                                            receivePacket.getPort());
                        socket.send(sendPacket);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        System.out.println("错误信息回传失败");
                        e.printStackTrace();
                    }


                }
                /** 满足要求 正常转发 */
                else{
                    int destinationPort=accountInfo.getCurrentPort();
                    MyPacket myPacket=new MyPacket(4,origin,receiveData.getMessage());
                    byte[] data2=MyPacket.toByte(myPacket);
                    try {
                        DatagramPacket sendPacket=new DatagramPacket(data2,data2.length,InetAddress.getLocalHost(),
                                                                    destinationPort);

                        socket.send(sendPacket);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        System.out.println("转发信息时发生错误");
                        e.printStackTrace();
                    }
                    displayMessage(receivePacket,"信息已发往"+destination);
                }
//                displayMessage(receivePacket,);
//                System.out.println("未处理的数据包类型");
            }

        }
    }
}

/** 存有某个账号的密码，以及好友的名称和端口号 */
class AccountInformation{
    String password;
    int currentPort;
    Vector<String> friends;
    AccountInformation(String password1){
        password=password1;
        currentPort=-1;     //未更新时端口号为-1，表示该用户不在线
        friends=new Vector<>();
    }
    void addFriend(String person){
        friends.add(person);
    }
    String getPassword(){
        return password;
    }
    /** 设置用户当前端口号 */
    int setCurrentPort(int port){
        currentPort=port;
        return currentPort;
    }

    /** 返回该用户当前端口号 */
    int getCurrentPort(){
        return currentPort;
    }
}


