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
    SUBJECT_CLAUSE("subjectClause", 1),

    /**
     * 宾语从句
     */
    OBJECT_CLAUSE("objectClause", 2),

    /**
     * 定语从句或同位语从句
     */
    ATTRIBUTIVE_CLAUSE_OR_APPOSITIVE_CLAUSE("attributiveClause", 3),

    /**
     * 表语从句
     */
    PREDICATIVE_CLAUSE("predicativeClause", 4),

    /**
     * 状语从句
     */
    ADVERBIAL_CLAUSE("adverbialClause", 5),

    /**
     * 被动句
     */
    PASSIVE_VOICE("passiveVoice", 6),

    /**
     * 双宾语句
     */
    DOUBLE_OBJECT("doubleObject", 7);

    /**
     * 句子类型的名称
     */
    private String typeName;

    /**
     * 句子类型标号
     * 1——主语从句；2——宾语从句；3——定语从句；4——表语从句；5——状语从句；6——同位语从句；7——被动句；8——双宾语句
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

    private String getTypeName() {
        return typeName;
    }

}
