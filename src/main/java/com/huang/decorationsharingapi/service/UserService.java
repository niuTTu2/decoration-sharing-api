package com.huang.decorationsharingapi.service;


import com.huang.decorationsharingapi.dto.request.RegisterRequest;
import com.huang.decorationsharingapi.dto.request.UpdateProfileRequest;
import com.huang.decorationsharingapi.entity.User;
import com.huang.decorationsharingapi.exception.ResourceNotFoundException;
import com.huang.decorationsharingapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;//注入 UserRepository，用于用户数据的数据库操作
    private final PasswordEncoder passwordEncoder;//// 注入 PasswordEncoder，用于密码加密

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    public boolean existsByUsername(String username) {//
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {//使用Builder模式创建一个新的User对象
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .avatar("https://joeschmoe.io/api/v1/" + registerRequest.getUsername())
                .role(User.Role.USER)//默认角色为USER
                .status(User.Status.ACTIVE)//默认状态为ACTIVE
                .build();

        return userRepository.save(user);//存入数据库中
    }

    @Transactional
    public User updateProfile(String username, UpdateProfileRequest updateRequest) {
        User user = findByUsername(username);

        if (updateRequest.getUsername() != null && !updateRequest.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateRequest.getUsername())) {
                throw new IllegalArgumentException("用户名已被使用");
            }
            user.setUsername(updateRequest.getUsername());
        }

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new IllegalArgumentException("邮箱已被使用");
            }
            user.setEmail(updateRequest.getEmail());
        }

        if (updateRequest.getAvatar() != null) {
            user.setAvatar(updateRequest.getAvatar());
        }

        if (updateRequest.getBio() != null) {
            user.setBio(updateRequest.getBio());
        }

        return userRepository.save(user);
    }
}