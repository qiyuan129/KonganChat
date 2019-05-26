import java.io.*;
import java.nio.ByteBuffer;

public class MyPacket implements Serializable{
    /** 0代表客户端发起的登录请求，
     * 1代表服务器返回的登录成功信息，
     * 2表示服务器返回的登陆失败信息
     * 3表示对某位好友发出地聊天信息
     * 4表示服务器向某个人转发的信息
     * 5表示服务器聊天信息的处理结果信息（比如查到对方不在线时返回错误）*/
    int status;

    String account;
    String password;
    String[] friends;
    /** 信息来源地 */
    String origin;
    /** 信息的目的地 */
    String destination;
    String message;
    ByteBuffer bytes;

    /** 发送登录请求时使用的构造函数 (状态码0）
     *   服务器转发信息的构造函数 （状态码4）
     *   需要接收的来自好友的信息 （状态码6）*/
    MyPacket(int status1,String str1,String str2){
        status=status1;
        if(status1==0) {
            account = str1;
            password = str2;


        }
        else if(status1==4){
            origin=str1;
            message=str2;
        }

    }

    /** 登录成功时使用的构造函数 （状态码1） */
    MyPacket(int status1,String[] friends1){
        status=status1;
        friends=friends1;
    }

    /** 登陆失败时使用的构造函数 （状态码2）
     *  发送信息失败时的构造函数 （状态码5）*/
    MyPacket(int status1){
        status=status1;
    }

    /** 向好友发送信息时使用的构造函数 （状态码3）*/
    MyPacket(int status1,String origin1,String destination1,String message1){
        status=status1;
        destination=destination1;
        origin = origin1;
        message=message1;
    }

    /** 序列化对象 方便发送 */
    static byte[] toByte(MyPacket packet){
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(packet);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /** 反序列化对象 方便读取 */
    static MyPacket decodeByte(byte[] bytes1){
        MyPacket obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (bytes1);
            ObjectInputStream ois = new ObjectInputStream (bis);
            obj = (MyPacket)ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    /** 获取该对象对应的账号 */
    String getAccount(){
        return account;
    }

    /** 获取该对象对应账号的密码 */
    String getPassword(){
        return password;
    }

    /** 返回代表该packet中数据内容的类型的int值status */
    int getStatus(){
        return status;
    }

    /** 返回好友信息数组 */
    String[] getFriends(){
        return friends;
    }

    /** 返回信息发送者的账号名 */
    String getOrigin(){
        return origin;
    }

    /** 返回信息目的地的账户名称 */
    String getDestination(){
        return destination;
    }

    /**返回要聊天过程中发送的信息*/
    String getMessage(){
        return message;
    }
}
