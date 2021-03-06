package edu.miu.cs.cs544.EAProject.service.impl;

import edu.miu.cs.cs544.EAProject.domain.*;
import edu.miu.cs.cs544.EAProject.dto.*;
import edu.miu.cs.cs544.EAProject.error.ClientException;
import edu.miu.cs.cs544.EAProject.i18n.DefaultMessageSource;
import edu.miu.cs.cs544.EAProject.repository.UserRepository;
import edu.miu.cs.cs544.EAProject.service.AccountRegistrationService;
import edu.miu.cs.cs544.EAProject.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AccountRegistrationServiceImpl implements AccountRegistrationService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final MessageSourceAccessor messages = DefaultMessageSource.getAccessor();

    @Autowired
    private AddressService addressService;

    @Override
    public UserDetailsDto registerStudent(int userId, String studentId, String name, String email,
                                          Integer mailingAddressId, Integer homeAddressId) {
        try {
            Address mailingAddress = addressService.getAddressId(mailingAddressId);
            Address homeAddress = addressService.getAddressId(homeAddressId);
            if (mailingAddress.getPostalCode() == null || homeAddress.getPostalCode() == null)
                throw new ClientException(messages.getMessage("error.generic.message"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.getMessage("error.user.notFound")));

            boolean isAlreadyStudent = user.getRoles().stream().anyMatch(role -> role instanceof Student);

            if (!isAlreadyStudent) {
                Student student = new Student(studentId, name, email, mailingAddress, homeAddress);
                user.addRole(student);
                user = userRepository.save(user);
            }
            return toUserDetailsDto(user);
        } catch (Exception e) {
            throw new ClientException(messages.getMessage("error.generic.message"));
        }

    }

    @Override
    public UserDetailsDto registerFaculty(int userId, String name, String email, String title) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.getMessage("error.user.notFound")));

        boolean isAlreadyFaculty = user.getRoles().stream().anyMatch(role -> role instanceof Faculty);

        if (!isAlreadyFaculty) {
            Faculty faculty = new Faculty(name, email, title);
            user.addRole(faculty);
            user = userRepository.save(user);
        }
        return toUserDetailsDto(user);
    }

    @Override
    public UserDetailsDto registerAdmin(int userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.getMessage("error.user.notFound")));

        boolean isAlreadyAdmin = user.getRoles().stream().anyMatch(role -> role instanceof Admin);

        if (!isAlreadyAdmin) {
            Admin admin = new Admin();
            user.addRole(admin);
            user = userRepository.save(user);
        }

        return toUserDetailsDto(user);
    }

    @Override
    public UserDetailsDto findUserById(int userId) {

        return userRepository.findById(userId)
                .map(this::toUserDetailsDto)
                .orElse(null);
    }

    private UserDetailsDto toUserDetailsDto(User user) {

        List<RoleDto> roles = user.getRoles().stream()
                .map(role -> {

                    RoleDto roleDto;

                    if (role instanceof Student) roleDto = modelMapper.map(role, StudentDto.class);
                    else if (role instanceof Faculty) roleDto = modelMapper.map(role, FacultyDto.class);
                    else if (role instanceof Admin) roleDto = modelMapper.map(role, AdminDto.class);
                    else throw new RuntimeException(messages.getMessage("error.user.notFound"));

                    roleDto.setUserId(user.getId());
                    return roleDto;
                })
                .collect(Collectors.toList());

        return new UserDetailsDto(user.getId(), user.getUsername(), roles);
    }
}
