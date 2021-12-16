package edu.miu.cs.cs544.EAProject.service.impl;

import edu.miu.cs.cs544.EAProject.domain.*;
import edu.miu.cs.cs544.EAProject.dto.*;
import edu.miu.cs.cs544.EAProject.repository.StudentRegistrationRepository;
import edu.miu.cs.cs544.EAProject.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentRegistrationServiceImpl implements StudentRegistrationService {

    @Autowired
    private StudentRegistrationRepository repository;
    @Autowired
    private FacultyService facultyService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private AcademicBlockService blockService;
    @Autowired
    private RegistrationRequestService registrationRequestService;
    @Autowired
    private CourseOfferingService courseOfferingService;

    @Override
    public List<StudentRegistrationDto> getRegistrationListByStudentId(Integer id) {
        List<StudentRegistrationDto> dtos = new ArrayList<>();
        Student student = repository.getById(id);
        List<CourseOffering> offerings = student.getCourseOfferings().stream().collect(Collectors.toList());
        for (CourseOffering offering : offerings) {
            Faculty faculty = facultyService.getFaultyById(offering.getFaculty().getId());
            Course course = courseService.getCourseById(offering.getCourse().getId());
            AcademicBlock block = blockService.getAcademicBlockById(offering.getAcademicBlock().getId());
            StudentRegistrationDto dto = new StudentRegistrationDto(block.getName(),
                    course.getCode(), course.getName(), faculty.getName(), block.getTimespan().getCreatedDate());
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public StudentRegistrationEventGroupDto getRegistrationListDto(Integer id) {
        Student student = repository.getById(id);

        StudentRegistrationEventGroupDto dto = new StudentRegistrationEventGroupDto();
        dto.setStudentName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setRegistrationGroupDtos(convertRegistrationGroupDto(student.getRegistrationGroups().stream().toList()));

        return dto;
    }

    @Override
    public ResponseEntity<String> saveRegistrationRequest(List<RegistrationRequestDto> requestDtos) {
        int priority = 1;
        List<RegistrationRequest> requests = new ArrayList<>();
        for (RegistrationRequestDto dto : requestDtos) {
            RegistrationRequest registrationRequest =
                    registrationRequestService.getRegistrationRequestsByStudentIdEventIdOfferingId(
                            dto.getStudentId(), dto.getRegistrationEventId(), dto.getCourseOfferingId()
                    );

            RegistrationRequest request = new RegistrationRequest();
            request.setPriority(priority++);

            Student student = repository.getById(dto.getStudentId());
            request.setStudent(student);

            CourseOffering offering = courseOfferingService.getCourseOfferingById(dto.getCourseOfferingId());
            request.setCourseOffering(offering);


            if (registrationRequest != null) request.setId(registrationRequest.getId());

            requests.add(request);
        }
        registrationRequestService.saveRegistrationRequest(requests);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully submit Registration Request");
    }

    private List<RegistrationGroupDto> convertRegistrationGroupDto(List<RegistrationGroup> groups) {
        List<RegistrationGroupDto> groupDtos = new ArrayList<>();
        for (RegistrationGroup group : groups) {
            RegistrationGroupDto groupDto = new RegistrationGroupDto();
            groupDto.setName(group.getName());
            groupDto.setId(group.getId());
            RegistrationEvent event = group.getRegistrationEvent();
            groupDto.setRegistrationEventDto(new RegistrationEventDto(event.getId(), event.getName(),
                    event.getStartEndDate().getCreatedDate(), event.getStartEndDate().getModifiedDate()));
            groupDto.setAcademicBlockDtos(convertAcademicBlockDto(group.getAcademicBlocks().stream().toList()));
            groupDtos.add(groupDto);
        }
        return groupDtos;
    }

    private List<AcademicBlockDto> convertAcademicBlockDto(List<AcademicBlock> blocks) {
        List<AcademicBlockDto> blockDtos = new ArrayList<>();
        for (AcademicBlock block : blocks) {
            AcademicBlockDto blockDto = new AcademicBlockDto();
            blockDto.setId(blockDto.getId());
            blockDto.setCode(block.getCode());
            blockDto.setName(block.getName());
            blockDto.setSemester(block.getSemester());
            blockDto.setStartDate(block.getAudit().getCreatedDate());
            blockDto.setEndDate(block.getAudit().getModifiedDate());
            blockDto.setCourseOfferingDtos(convertCourseOfferingDto(block.getCourseOfferings().stream().toList(), block.getId()));
            blockDtos.add(blockDto);
        }
        return blockDtos;
    }

    private List<CourseOfferingDto> convertCourseOfferingDto(List<CourseOffering> offerings, int blockId) {
        List<CourseOfferingDto> courseOfferingDtos = new ArrayList<>();
        for (CourseOffering offering : offerings) {
            Course course = offering.getCourse();
            Faculty faculty = offering.getFaculty();

            CourseOfferingDto courseOfferingDto = new CourseOfferingDto();
            courseOfferingDto.setCourseOfferingCode(offering.getCode());
            courseOfferingDto.setCourseOfferingId(offering.getId());
            courseOfferingDto.setCapacity(offering.getCapacity());
            courseOfferingDto.setFacultyInitials(offering.getFacultyInitials());
            courseOfferingDto.setCourseId(course.getId());
            courseOfferingDto.setAcademicBlockId(blockId);
            courseOfferingDto.setCourseCode(course.getCode());
            courseOfferingDto.setCourseName(course.getName());
            courseOfferingDto.setFacultyId(faculty.getId());
            courseOfferingDto.setFacultyName(faculty.getName());

            courseOfferingDtos.add(courseOfferingDto);
        }
        return courseOfferingDtos;
    }
}
