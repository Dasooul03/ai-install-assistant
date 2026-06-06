package com.example.installassistant.intent;

/**
 * 指令意图类型枚举
 */
public enum IntentType {

    /** 知识问答 — 安装手册、FAQ 等 */
    KNOWLEDGE_QA,

    /** 安装指引 — 分步安装操作 */
    INSTALL_GUIDE,

    /** 创建集群 */
    CREATE_CLUSTER,

    /** 创建微服务分区 */
    CREATE_PARTITION,

    /** 给微服务增加实例 */
    ADD_INSTANCE,

    /** 微服务启停管理 */
    SERVICE_LIFECYCLE,

    /** 故障诊断 */
    DIAGNOSTIC,

    /** 闲聊 / 兜底 */
    CHITCHAT
}
