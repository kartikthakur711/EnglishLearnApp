package com.kartik.englishapp.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "lesson_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lesson_id"}))
public class LessonProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    private int score;

    private LocalDate lastAttemptDate;

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public LocalDate getLastAttemptDate() { return lastAttemptDate; }
    public void setLastAttemptDate(LocalDate lastAttemptDate) { this.lastAttemptDate = lastAttemptDate; }
}
