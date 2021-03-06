package edu.miu.cs.cs544.EAProject.domain;

import edu.miu.cs.cs544.EAProject.domain.audit.Audit;
import edu.miu.cs.cs544.EAProject.domain.audit.AuditListener;
import edu.miu.cs.cs544.EAProject.domain.audit.Auditable;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditListener.class)
public class RegistrationRequest implements Auditable {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "courseOfferingId", nullable = false)
    private CourseOffering courseOffering;

    @Column(nullable = false)
    private int priority;

    @Embedded
    private Audit audit;

    public RegistrationRequest(Student student, CourseOffering courseOffering, int priority) {
        this.student = student;
        this.courseOffering = courseOffering;
        this.priority = priority;
    }

    public RegistrationRequest(Student student, int priority) {
        this.student = student;
        this.priority = priority;
    }
}
