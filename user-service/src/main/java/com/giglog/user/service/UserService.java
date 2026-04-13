package com.giglog.user.service;

import com.giglog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void signUp() {
        // 회원가입 로직 뼈대
    }

    public void login() {
        // 로그인 로직 뼈대
    }
}
