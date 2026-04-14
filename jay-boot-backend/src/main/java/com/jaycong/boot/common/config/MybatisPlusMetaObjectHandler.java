package com.jaycong.boot.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.jaycong.boot.modules.auth.context.LoginContext;
import com.jaycong.boot.modules.auth.context.LoginPrincipal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    private final LoginContext loginContext;

    public MybatisPlusMetaObjectHandler(LoginContext loginContext) {
        this.loginContext = loginContext;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LoginPrincipal principal = resolvePrincipal().orElse(null);
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "creatorId", Long.class, principal == null ? 0L : principal.userId());
        this.strictInsertFill(metaObject, "creatorName", String.class, principal == null ? "system" : principal.username());
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updaterId", Long.class, principal == null ? 0L : principal.userId());
        this.strictInsertFill(metaObject, "updaterName", String.class, principal == null ? "system" : principal.username());
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LoginPrincipal principal = resolvePrincipal().orElse(null);
        this.strictUpdateFill(metaObject, "updaterId", Long.class, principal == null ? 0L : principal.userId());
        this.strictUpdateFill(metaObject, "updaterName", String.class, principal == null ? "system" : principal.username());
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
    }

    private Optional<LoginPrincipal> resolvePrincipal() {
        return loginContext.currentPrincipal();
    }
}
