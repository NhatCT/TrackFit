package com.ntn.repositories.impl;

import com.ntn.pojo.Exercises;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query; // Lưu ý import đúng package Hibernate
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExercisesRepositoryImplTest {

    @Mock
    private LocalSessionFactoryBean factory;

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Exercises> query;

    @InjectMocks
    private ExercisesRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        // Cấu hình chuỗi Mock để lấy được Session:
        // factory.getObject() -> sessionFactory
        // sessionFactory.getCurrentSession() -> session
        when(factory.getObject()).thenReturn(sessionFactory);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    @DisplayName("Test Save (Create): Should call persist when ID is null")
    void save_ShouldCallPersist_WhenIdIsNull() {
        // GIVEN
        Exercises exercise = new Exercises();
        exercise.setExercisesId(null); // ID null -> Create mới
        exercise.setName("Squat");

        // WHEN
        Exercises result = repository.save(exercise);

        // THEN
        // Kiểm tra xem hàm persist có được gọi không
        verify(session, times(1)).persist(exercise);
        // Kiểm tra hàm merge KHÔNG được gọi
        verify(session, never()).merge(any());
        assertEquals(exercise, result);
    }

    @Test
    @DisplayName("Test Save (Update): Should call merge when ID is not null")
    void save_ShouldCallMerge_WhenIdIsNotNull() {
        // GIVEN
        Exercises exercise = new Exercises();
        exercise.setExercisesId(10); // Đã có ID -> Update
        exercise.setName("Squat Updated");

        // Mock hành vi của merge: trả về chính object đó (hoặc bản copy)
        when(session.merge(exercise)).thenReturn(exercise);

        // WHEN
        Exercises result = repository.save(exercise);

        // THEN
        // Kiểm tra xem hàm merge có được gọi không
        verify(session, times(1)).merge(exercise);
        // Kiểm tra hàm persist KHÔNG được gọi
        verify(session, never()).persist(any());
        assertEquals(exercise, result);
    }

    @Test
    @DisplayName("Test FindById: Should call session.get")
    void findById_ShouldReturnEntity() {
        // GIVEN
        int id = 1;
        Exercises expectedExercise = new Exercises();
        expectedExercise.setExercisesId(id);

        when(session.get(Exercises.class, id)).thenReturn(expectedExercise);

        // WHEN
        Exercises result = repository.findById(id);

        // THEN
        assertNotNull(result);
        assertEquals(id, result.getExercisesId());
        verify(session, times(1)).get(Exercises.class, id);
    }

    @Test
    @DisplayName("Test FindAll: Should create NamedQuery and return list")
    void findAll_ShouldReturnList() {
        // GIVEN
        List<Exercises> expectedList = Collections.singletonList(new Exercises());
        
        // Mock chuỗi gọi Query:
        // session.createNamedQuery(...) -> query
        // query.getResultList() -> list
        when(session.createNamedQuery("Exercises.findAll", Exercises.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedList);

        // WHEN
        List<Exercises> result = repository.findAll();

        // THEN
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(session).createNamedQuery("Exercises.findAll", Exercises.class);
    }
}
