package edu.miu.cs.cs544.EAProject.service;

import edu.miu.cs.cs544.EAProject.advice.EventNotFoundException;
import edu.miu.cs.cs544.EAProject.domain.RegistrationEvent;
import edu.miu.cs.cs544.EAProject.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Comparator;

@Transactional
@Service
public class EventServiceImpl implements EventService{

    @Autowired
    EventRepository repository;

    @Override
    public Page<RegistrationEvent> getAllEvents(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public RegistrationEvent getEventById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new EventNotFoundException());
    }

    @Override
    public RegistrationEvent createEvent(RegistrationEvent event) {
        return repository.save(event);
    }

    @Override
    public RegistrationEvent updateEvent(RegistrationEvent event) {
       RegistrationEvent registrationEvent = getEventById(event.getId());
       registrationEvent.setRegistrationRequests(event.getRegistrationRequests());
       registrationEvent.setRegistrationGroups(event.getRegistrationGroups());
       registrationEvent.setName(event.getName());
       registrationEvent.setStartEndDate(event.getStartEndDate());

       return  repository.save(registrationEvent);
    }

    @Override
    public void deleteEvent(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public RegistrationEvent getLatestEvent() {
        return repository.findAll().stream().
                sorted(Comparator.nullsLast(
                        (e1, e2) -> e2.getStartEndDate().getCreatedDate().compareTo(e1.getStartEndDate().getCreatedDate())
                )).findFirst().orElseThrow(() -> new EventNotFoundException());
    }


}
