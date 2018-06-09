import com.taobao.jm.msgcenter.MsgManager;
import com.taobao.jm.msgcenter.common.MsgConstants;
import com.taobao.jm.msgcenter.common.Result;


/**
 * @author shuohai.lhl@taobao.com
 *
 * @time May 29, 2013 5:22:35 PM
 *
 * @desc 
 */
public class AlertTest {
    public static void main(String[] args){
        MsgManager mcm = new MsgManager();
        //=================configServer使用方式====================
        //      mcm.setSerivceVersion("1.0.0.daily");
        //      mcm.init();
        //==================custom 指定serverAddr方式==============
        mcm.setServiceType(MsgConstants.CUSTOM_SERVER_LIST_TYPE);
        mcm.setCustomServerHosts("ops.jm.taobao.org:9999");
        mcm.init();
        //发送消息部分
        String address = "18668188128";
        String subject = "title:测试标题";
        String content = "content:测试内容 IC-DATA-1";
        String channel = MsgManager.MsgType.SMS_TYPE;

        String sourceId = "yugong*yugong";
        String templateId = "168849275";
        String messageTypeId = "176904884";

        Result result = mcm.sendMsg(address, subject, content, channel, sourceId, templateId, messageTypeId);
        System.out.println(result.toString());
    }
}
