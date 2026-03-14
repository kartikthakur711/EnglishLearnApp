package com.kartik.englishapp.repository;

import com.kartik.englishapp.model.Lesson;
import com.kartik.englishapp.model.LessonProgress;
import com.kartik.englishapp.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUserAndLesson(UserAccount user, Lesson lesson);
    List<LessonProgress> findByUser(UserAccount user);
}
