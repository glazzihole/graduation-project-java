package com.hugailei.graduation.corpus.enums;

/**
 * @author HU Gailei
 * @date 2018/11/27
 * <p>
 * description: 句子类型相关枚举
 * </p>
 **/
public enum SentencePatternType {
    /**
     * 主语从句
     */
    SUBJECT_CLAUSE("主语从句", 1),

    /**
     * 宾语从句
     */
    OBJECT_CLAUSE("宾语从句", 2),

    /**
     * 定语从句或同位语从句
     */
    ATTRIBUTIVE_CLAUSE_OR_APPOSITIVE_CLAUSE("定语从句或同位语从句", 3),

    /**
     * 表语从句
     */
    PREDICATIVE_CLAUSE("表语从句", 4),

    /**
     * 状语从句
     */
    ADVERBIAL_CLAUSE("状语从句", 5),

    /**
     * 被动句
     */
    PASSIVE_VOICE("被动句", 6),

    /**
     * 双宾语句
     */
    DOUBLE_OBJECT("双宾语句", 7),

    /**
     * so/such that句型
     */
    S_THAT("so/such that句型", 8),

    /**
     * too to句型
     */
    TOO_TO("too to句型", 9),

    /**
     * 倒装句型
     */
    INVERTED_STRUCTURE("倒装句型", 10),

    /**
     * 强调句型
     */
    EMPHATIC_STRUCTURE("强调句型", 11);

    /**
     * 句子类型的名称
     */
    private String typeName;

    /**
     * 句子类型标号
     * 1——主语从句；2——宾语从句；3——定语从句/同位语从句；4——表语从句；5——状语从句；6——被动句；7——双宾语句；8——so/such that句型
     */
    private int type;

    /**
     * 构造方法
     * @param typeName
     * @param type
     */
    SentencePatternType(String typeName, int type){
        this.typeName = typeName;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getTypeName() {
        return typeName;
    }

}
