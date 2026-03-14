package com.kartik.englishapp.repository;

import com.kartik.englishapp.model.ChatEntry;
import com.kartik.englishapp.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatEntryRepository extends JpaRepository<ChatEntry, Long> {
    List<ChatEntry> findTop20ByUserOrderByCreatedAtDesc(UserAccount user);
}
