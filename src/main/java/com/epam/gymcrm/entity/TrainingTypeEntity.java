package com.epam.gymcrm.entity;

import com.epam.gymcrm.model.TrainingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "training_types")
@Getter
@Setter
@EqualsAndHashCode(of = "typeName")
public class TrainingTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_name", unique = true, nullable = false, length = 50)
    private TrainingType typeName;
}
