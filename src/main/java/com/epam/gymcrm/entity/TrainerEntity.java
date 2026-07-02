package com.epam.gymcrm.entity;

import com.epam.gymcrm.model.TrainingType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainers")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, exclude = {"trainees", "trainings", "specialization"})
public class TrainerEntity extends UserEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingTypeEntity specialization;

    @ManyToMany(mappedBy = "trainers", fetch = FetchType.LAZY)
    private Set<TraineeEntity> trainees = new HashSet<>();

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    private Set<TrainingEntity> trainings = new HashSet<>();

    public boolean matchesSpecialization(TrainingType type) {
        return type != null && specialization != null
                && type.name().equalsIgnoreCase(specialization.getTypeName());
    }
}
