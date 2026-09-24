package com.rehab.system.common;

public final class SecurityExpressions {

    /** 基础数据（医生/患者/床位/项目）的写操作：管理员或科室管理员 */
    public static final String WRITE_BASE = "hasAnyRole('ADMIN','DEPT_ADMIN')";

    /** 评估与治疗记录的写操作：管理员、科室管理员、医师、治疗师 */
    public static final String WRITE_RECORD = "hasAnyRole('ADMIN','DEPT_ADMIN','DOCTOR','THERAPIST')";

    /** 仅系统管理员 */
    public static final String ADMIN_ONLY = "hasRole('ADMIN')";

    private SecurityExpressions() {
    }
}
