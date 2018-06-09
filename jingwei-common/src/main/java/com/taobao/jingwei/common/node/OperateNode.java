package com.taobao.jingwei.common.node;

import com.alibaba.common.lang.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 节点的总控配置存储节点，负责启停AGENT,TASK 具体数据的监听由agent或者task自己负责，当
 * 接受到关闭状态数据后，由agent或者task自己 安全关闭自己
 *
 * @author qihao
 */
public class OperateNode extends AbstractNode {
    /**
     * 该OperateNode拥有者的节点，或拥有者的dataID 需要自己手工set,该属性无需存入zk或者diamond
     * agent或者task只要写死固定获取其自己目录下的 /operate即可
     */
    private String ownerDataIdOrPath;


    private OperateEnum operateEnum;

    /**
     * json存储数据key定义
     */
    private final static String OPERATE_KEY = "operate";

    /*
      * 将OperateNode 自己特殊属性设置到JSONObeject中
      *
      * @see com.taobao.jingwei.common.node.AbstractNode#
      * specilizeAttributeToJsonObject(org.json.JSONObject)
      */
    @Override
    protected void specilizeAttributeToJsonObject(
            JSONObject jsonObject) throws JSONException {
        if (operateEnum == null) {
            jsonObject.put(OPERATE_KEY, StringUtil.EMPTY_STRING);
        } else {
            jsonObject.put(OPERATE_KEY,
                    StringUtil.defaultIfBlank(operateEnum.getOperateString()));
        }
    }

    /**
     * 将JSONObject中节点OperateNode特殊的属性值，填充到自己对应属性中
     *
     * @see com.taobao.jingwei.common.node.AbstractNode#jsonObjectToSpecilizeAttribute(org.json.JSONObject)
     */
    @Override
    protected void jsonObjectToSpecilizeAttribute(
            JSONObject jsonObject) throws JSONException {
        this.operateEnum = OperateEnum.getOperateEnumByString(jsonObject
                .getString(OPERATE_KEY));
    }

    public boolean isPersistent() {
        return true;
    }

    public String getDataIdOrNodePath() {
        return OperateNode.getDataIdOrNodePathByOwner(ownerDataIdOrPath);
    }

    public static String getDataIdOrNodePathByOwner(String ownerDataIdOrPath) {
        if (StringUtil.isBlank(ownerDataIdOrPath)) {
            return StringUtil.EMPTY_STRING;
        }
        return ownerDataIdOrPath + ZK_PATH_SEP + JINGWEI_OPERATE_NODE_NAME;
    }

    public void setOwnerDataIdOrPath(String ownerDataIdOrPath) {
        this.ownerDataIdOrPath = ownerDataIdOrPath;
    }

    public void setOperateEnum(OperateEnum operateEnum) {
        this.operateEnum = operateEnum;
    }

    public OperateEnum getOperateEnum() {
        return operateEnum;
    }

    /**
     * 状态类型枚举
     *
     * @author qihao
     */
    public enum OperateEnum {
        /**
         * 控制节点的启动控制值
         */
        NODE_START("Start"),

        NODE_STOP("Stop");

        private String operateString;

        OperateEnum(String operateString) {
            this.operateString = operateString;
        }

        public String getOperateString() {
            return operateString;
        }

        /**
         * 根据operateString获取Operate的enum对象
         *
         * @param operateString 操作字符串
         * @return 对应操作字符串的枚举类
         */
        public static OperateEnum getOperateEnumByString(String operateString) {
            if (StringUtil.isBlank(operateString)) {
                return null;
            }
            OperateEnum retEnum = null;
            for (OperateEnum operateEnum : OperateEnum.values()) {
                if (StringUtil.equals(operateEnum.getOperateString(),
                        operateString)) {
                    retEnum = operateEnum;
                    break;
                }
            }
            return retEnum;
        }
    }
}