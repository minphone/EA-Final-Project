package edu.miu.cs.cs544.EAProject.domain;

import edu.miu.cs.cs544.EAProject.domain.audit.Audit;
import edu.miu.cs.cs544.EAProject.domain.audit.AuditListener;
import edu.miu.cs.cs544.EAProject.domain.audit.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Entity
@Setter
@Getter
@NoArgsConstructor
@EntityListeners(AuditListener.class)
public class RegistrationEvent implements Auditable {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "createdDate", column = @Column(name = "startDate")),
            @AttributeOverride(name = "modifiedDate", column = @Column(name = "endDate"))
    })
    private Audit startEndDate;

    @Embedded
    private Audit audit;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id")
    private Collection<RegistrationGroup> registrationGroups;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "request_id")
    private Collection<RegistrationRequest> registrationRequests;

    @Transient
    EventStatus status = EventStatus.CLOSED;

    public void addGroup(RegistrationGroup group) {
        if (group != null) {
            registrationGroups.add(group);
        }
    }

    public void addRequest(RegistrationRequest request) {
        if (request != null) {
            registrationRequests.add(request);
        }
    }

    public EventStatus getStatus() {
      if(isEventOpen()){
          this.status = EventStatus.OPEN;
      }
      return this.status;
    }

   public Boolean isEventOpen(){
       return ChronoUnit.NANOS.between(LocalDateTime.now(), this.startEndDate.getModifiedDate()) > 0 ? true: false;
   }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartEndDate(Audit startEndDate) {
        this.startEndDate = startEndDate;
    }

    public void setRegistrationGroups(Collection<RegistrationGroup> registrationGroups) {
        this.registrationGroups = registrationGroups;
    }

    public void setRegistrationRequests(Collection<RegistrationRequest> registrationRequests) {
        this.registrationRequests = registrationRequests;
    }
}
