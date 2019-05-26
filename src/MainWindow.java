import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MainWindow extends JFrame{
    JTabbedPane friendList=new JTabbedPane(SwingConstants.LEFT);
    DatagramSocket socket;
    /** 使用当前窗口的用户名*/
    String username;
    String[] friends;
    MainWindow(DatagramSocket socket1, String[] friends1,String username1){
        this.setTitle("空安聊----主界面");
        socket=socket1;
        username=username1;
        friends=friends1;
        this.add(friendList);

        System.out.println("接收到了"+friends1.length+"位好友的信息");
        for(int i = 0;i<friends1.length;i++) {
            String friend=friends1[i];
            ChatPanel chatPanel=new ChatPanel(socket,username,friend);
            friendList.addTab(friend,chatPanel);
        }

        addReceiveListener();

        this.setSize(700,700);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }
    void addReceiveListener(){
        Runnable r= new Runnable() {
            @Override
            public void run() {
                while(true){
                    byte[] data=new byte[512];
                    DatagramPacket receivePacket=new DatagramPacket(data,data.length);

                    //接收数据包
                    try {
                        socket.receive(receivePacket);
                    } catch (IOException e) {
                        System.out.println("接收数据包时出现问题");
                        e.printStackTrace();
                    }

                    //解码数据包
                    MyPacket receiveData=MyPacket.decodeByte(receivePacket.getData());
                    int type=receiveData.getStatus();    //获得数据报数据类型
                    /** 处理服务器返回的信息发送失败信息 */
                    if(type==5){
                        JOptionPane.showMessageDialog(null,"该好友不在线，信息发送失败");
                    }
                    /** 消息接收正常*/
                    else if(type==4){
                        String origin=receiveData.getOrigin();
                        ChatPanel chatPanel1;
                        int tabIndex=friendList.indexOfTab(origin);
                        if(tabIndex==-1){
                            JOptionPane.showMessageDialog(null,"获取发送信息的好友所在的选项卡index时" +
                                    "出现了错误！");
                        }
                        else{
                            System.out.println("信息接收预备工作正常，准备显示信息");
                            //选中发信息的好友的选项卡
                            friendList.setSelectedIndex(tabIndex);

                            //调用对应的chatPanel的receiveMessage方法打印出接收到的信息
                            chatPanel1=(ChatPanel) friendList.getSelectedComponent();
                            chatPanel1.showMessage(receiveData.getMessage());

                        }
                    }

                    //为了方便调试，在控制台输出内容
//                    displayMessage(receivePacket);
                }
            }
        };
        Thread thread=new Thread(r);
        thread.start();
    }
}
