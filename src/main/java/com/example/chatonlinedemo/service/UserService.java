package com.example.chatonlinedemo.service;

import com.example.chatonlinedemo.dto.UpdateProfileRequest;
import com.example.chatonlinedemo.dto.UserDTO;
import com.example.chatonlinedemo.entity.User;
import com.example.chatonlinedemo.exception.ResourceNotFoundException;
import com.example.chatonlinedemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserDTO.fromEntity(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return UserDTO.fromEntity(user);
    }

    public UserDTO getCurrentUser(String username) {
        return getUserByUsername(username);
    }

    @Transactional
    public UserDTO updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getFacebookLink() != null) {
            user.setFacebookLink(request.getFacebookLink());
        }

        user = userRepository.save(user);
        return UserDTO.fromEntity(user);
    }

    @Transactional
    public void setUserOnline(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setOnline(true);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void setUserOffline(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setOnline(false);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<UserDTO> getOnlineUsers() {
        return userRepository.findAllOnlineUsers().stream()
                .map(UserDTO::fromEntityBasic)
                .collect(Collectors.toList());
    }

    @Transactional
    public void blockUser(Long userId, Long blockedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocked user not found"));

        user.getBlockedUsers().add(blockedUser);
        userRepository.save(user);
    }

    @Transactional
    public void unblockUser(Long userId, Long blockedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.getBlockedUsers().remove(blockedUser);
        userRepository.save(user);
    }

    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
