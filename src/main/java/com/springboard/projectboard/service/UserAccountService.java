package com.springboard.projectboard.service;

import com.springboard.projectboard.domain.UserAccount;
import com.springboard.projectboard.dto.UserAccountDto;
import com.springboard.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public Optional<UserAccountDto> searchUser(String username) {
        return userAccountRepository.findById(username)
                .map(UserAccountDto::from);
    }

    // 인증이 없고 새로운 유저를 만들 때 사용하기 위한 UserAccount.of(...)를 사용!
    // 그래서 마지막에 createdBy 인 username 을 넣어준다.
    public UserAccountDto saveUser(String username, String password, String email, String nickname, String memo) {
        return UserAccountDto.from(
                userAccountRepository.save(UserAccount.of(username, password, email, nickname, memo, username))
        );
    }
}
