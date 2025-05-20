package com.huang.decorationsharingapi.controller;

import com.huang.decorationsharingapi.dto.request.PasswordUpdateRequest;
import com.huang.decorationsharingapi.dto.response.MessageResponse;
import com.huang.decorationsharingapi.dto.response.UserProfileResponse;
import com.huang.decorationsharingapi.entity.Favorite;
import com.huang.decorationsharingapi.entity.Material;
import com.huang.decorationsharingapi.entity.User;
import com.huang.decorationsharingapi.repository.FavoriteRepository;
import com.huang.decorationsharingapi.repository.MaterialRepository;
import com.huang.decorationsharingapi.repository.UserRepository;
import com.huang.decorationsharingapi.service.FileStorageService;
import com.huang.decorationsharingapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.security.Principal;
//处理用户个人资料相关的操作
@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final FavoriteRepository favoriteRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取用户详细个人资料，包括统计信息
     */
    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserProfile(Principal principal) {
        User user = userService.findByUsername(principal.getName());

        // 获取用户上传的素材数量
        long uploadCount = materialRepository.countByUser(user);

        // 获取用户收藏的素材数量
        long favoriteCount = favoriteRepository.countByUser(user);

        // 获取关注者数量（暂时返回0，需要后续实现关注功能）
        long followersCount = 0;

        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .uploadCount(uploadCount)
                .favoriteCount(favoriteCount)
                .followersCount(followersCount)
                .createdAt(user.getCreatedAt())
                .build());
    }

    /**
     * 上传用户头像
     */
    @PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        try {
            User user = userService.findByUsername(principal.getName());

            // 存储头像文件
            String avatarFileName = fileStorageService.storeFile(file);

            // 更新用户头像
            user.setAvatar(avatarFileName);
            userRepository.save(user);

            return ResponseEntity.ok().body(new MessageResponse("头像上传成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("头像上传失败: " + e.getMessage()));
        }
    }

    /**
     * 更新用户密码
     */
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(
            @Valid @RequestBody PasswordUpdateRequest passwordRequest,
            Principal principal) {

        User user = userService.findByUsername(principal.getName());

        // 验证当前密码
        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("当前密码不正确"));
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("密码更新成功"));
    }
}