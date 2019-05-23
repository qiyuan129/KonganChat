import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Server extends JFrame {
    private DatagramSocket socket;
//    private JTextField broadcast;
    private JTextArea eventLog;
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
            System.exit(1);
        }
        this.setSize(700,700);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    void waitForMessage(){
        while(true){
            try {
                byte data1[]=new byte[50];
                DatagramPacket receivePacket=new DatagramPacket(data1,data1.length);
                //接收信息
                socket.receive(receivePacket);
                //在服务器端的eventlog中显示接收到的信息
                displayMessage(receivePacket);

                String str = "Thank you for your message,sir♂";
                byte data2[]=str.getBytes();

                DatagramPacket responsePacket=new DatagramPacket(data2,data2.length,receivePacket.getAddress(),receivePacket.getPort());
                socket.send(responsePacket);

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    void displayMessage(DatagramPacket receivePacket){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run() // updates displayArea
                    {

                        eventLog.append(df.format(new Date())+"     FROM port："+receivePacket.getPort()+"\n");// new Date()为获取当前系统时间
                        eventLog.append("  "+new String(receivePacket.getData(),0,receivePacket.getLength())+"\n");
                    } // end method run
                } // end anonymous inner class
        ); //

//        repaint();
//        System.out.println(df.format(new Date())+"     FROM port："+receivePacket.getPort());// new Date()为获取当前系统时间
//
//        System.out.println(("  "+new String(receivePacket.getData(),0,receivePacket.getData().length)));
    }

}
