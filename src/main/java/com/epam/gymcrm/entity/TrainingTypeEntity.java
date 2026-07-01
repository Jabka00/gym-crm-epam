package com.epam.gymcrm.entity;

import com.epam.gymcrm.model.TrainingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "training_types")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class TrainingTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_name", unique = true, nullable = false, length = 50)
    private String typeName;

    public static TrainingTypeEntity of(TrainingType type) {
        TrainingTypeEntity entity = new TrainingTypeEntity();
        entity.setTypeName(type.name());
        return entity;
    }

    public TrainingType toEnum() {
        return TrainingType.valueOf(typeName);
    }
}
