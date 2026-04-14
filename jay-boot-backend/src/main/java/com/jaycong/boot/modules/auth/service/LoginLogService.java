package com.jaycong.boot.modules.auth.service;

import com.jaycong.boot.modules.auth.entity.LoginLogEntity;
import com.jaycong.boot.modules.auth.mapper.LoginLogMapper;
import org.springframework.stereotype.Service;

@Service
public class LoginLogService {

    private final LoginLogMapper loginLogMapper;

    public LoginLogService(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    public void record(Long userId, String ip, String ua, boolean success, String reason) {
        LoginLogEntity loginLog = new LoginLogEntity();
        loginLog.setUserId(userId);
        loginLog.setIp(ip);
        loginLog.setUa(ua);
        loginLog.setSuccess(success);
        loginLog.setReason(reason);
        loginLogMapper.insert(loginLog);
    }
}