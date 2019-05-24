import java.io.*;
import java.nio.ByteBuffer;

public class MyPacket implements Serializable{
    /** 0代表客户端发起的登录请求，
     * 1代表服务器返回的登录成功信息，
     * 2表示服务器返回的登陆失败信息 */
    int status;

    String account;
    String password;
    Friend[] friends;
    String message;
    ByteBuffer bytes;

    /** 发送登录请求时使用的构造函数 */
    MyPacket(int status1,String account1,String password1){
        status=status1;
        account=account1;
        password=password1;
    }

    /** 登录成功时使用的构造函数 */
    MyPacket(int status1,Friend[] friends1){
        status=status1;
        friends=friends1;
    }

    /** 登陆失败时使用的构造函数 */
    MyPacket(int status1){
        status=status1;
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
}
