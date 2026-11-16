package ng.sciCloud.library.service;

import ng.sciCloud.library.dto.RegisterForm;
import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.Role;
import ng.sciCloud.library.repository.UserRepository;
import ng.sciCloud.library.utils.validators.MemberValidation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberValidation validation;

    public MemberService(UserRepository userRepository, PasswordEncoder passwordEncoder, MemberValidation validation) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validation = validation;
    }

    @Transactional
    public AppUser register(RegisterForm form) {
        validation.validateRegistration(form);
        return createUser(form.getFullName(), form.getEmail(), form.getPassword(), Role.USER);
    }

    @Transactional
    public AppUser createUser(String fullName, String email, String rawPassword, Role role) {
        String cleanEmail = email.trim().toLowerCase();
        validation.validateEmailAvailable(cleanEmail);
        AppUser user = new AppUser();
        user.setFullName(fullName.trim());
        user.setEmail(cleanEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    public AppUser requireByEmail(String email) {
        return validation.requireAccountByEmail(email);
    }

    public AppUser get(Long id) {
        return validation.requireMember(id);
    }

    public List<AppUser> listMembers() {
        return userRepository.findByRoleOrderByFullNameAsc(Role.USER);
    }

    @Transactional
    public AppUser toggleEnabled(Long id) {
        AppUser member = get(id);
        validation.validateToggleEnabled(member);
        member.setEnabled(!member.isEnabled());
        return member;
    }
}
