package com.taobao.jingwei.common.node;

import com.alibaba.common.lang.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * �ڵ���ܿ����ô洢�ڵ㣬������ͣAGENT,TASK �������ݵļ�����agent����task�Լ����𣬵�
 * ���ܵ��ر�״̬���ݺ���agent����task�Լ� ��ȫ�ر��Լ�
 *
 * @author qihao
 */
public class OperateNode extends AbstractNode {
    /**
     * ��OperateNodeӵ���ߵĽڵ㣬��ӵ���ߵ�dataID ��Ҫ�Լ��ֹ�set,�������������zk����diamond
     * agent����taskֻҪд���̶���ȡ���Լ�Ŀ¼�µ� /operate����
     */
    private String ownerDataIdOrPath;


    private OperateEnum operateEnum;

    /**
     * json�洢����key����
     */
    private final static String OPERATE_KEY = "operate";

    /*
      * ��OperateNode �Լ������������õ�JSONObeject��
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
     * ��JSONObject�нڵ�OperateNode���������ֵ����䵽�Լ���Ӧ������
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
     * ״̬����ö��
     *
     * @author qihao
     */
    public enum OperateEnum {
        /**
         * ���ƽڵ����������ֵ
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
         * ����operateString��ȡOperate��enum����
         *
         * @param operateString �����ַ���
         * @return ��Ӧ�����ַ�����ö����
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