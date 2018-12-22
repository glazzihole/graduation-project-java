package com.hugailei.graduation.corpus.enums;

/**
 * @author HU Gailei
 * @date 2018/12/21
 * <p>
 * description: 主题类型相关枚举
 * </p>
 **/
public enum Topic {
    /**
     * 国际主题
     */
    WORLD("国际", 1),

    /**
     * 体育主题
     */
    SPORTS("体育", 2),

    /**
     * 商业主题
     */
    BUSINESS("商业", 3),

    /**
     * 科学与技术主题
     */
    SCI_OR_TECH("科学及技术", 4),

    /**
     * 其他主题
     */
    OTHER("其他", 5);

    private String topicName;
    private int topicNum;

    /**
     * 构造方法
     * @param topicName
     * @param topicNum
     */
    Topic(String topicName, int topicNum){
        this.topicName = topicName;
        this.topicNum = topicNum;
    }

    public int getTopicNum() {
        return topicNum;
    }

    public String getTopicName() {
        return topicName;
    }

    /**
     * 返回指定编号的'枚举'
     * @param topicNum
     * @return SharedObjTypeEnum
     * @throws
     */
    public static <T extends Topic> T getEnumBycode(Class<T> clazz, int topicNum) {
        for (T _enum : clazz.getEnumConstants()) {
            if (topicNum == _enum.getTopicNum()) {
                return _enum;
            }
        }
        return null;
    }

}
