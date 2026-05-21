package com.english.learning.shiro;

import com.english.learning.entity.User;
import com.english.learning.mapper.UserMapper;
import com.english.learning.util.PasswordService;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BcryptCredentialsMatcher extends SimpleCredentialsMatcher {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        char[] passwordChars = upToken.getPassword();
        if (passwordChars == null) {
            return false;
        }
        String rawPassword = new String(passwordChars);
        String storedPassword = (String) info.getCredentials();
        User user = (User) info.getPrincipals().getPrimaryPrincipal();

        if (!passwordService.matches(rawPassword, storedPassword)) {
            return false;
        }

        if (!passwordService.isEncoded(storedPassword)) {
            user.setPassword(passwordService.encode(rawPassword));
            userMapper.updateById(user);
        }
        return true;
    }
}
