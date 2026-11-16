package ng.sciCloud.library.service;

import ng.sciCloud.library.IntegrationTestBase;
import ng.sciCloud.library.dto.RegisterForm;
import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberServiceTest extends IntegrationTestBase {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterForm form(String name, String email, String password, String confirm) {
        RegisterForm form = new RegisterForm();
        form.setFullName(name);
        form.setEmail(email);
        form.setPassword(password);
        form.setConfirmPassword(confirm);
        return form;
    }

    @Test
    void registerCreatesEnabledUserWithHashedPassword() {
        AppUser user = memberService.register(form("  Ada Obi ", "  Ada@Example.com ", "Secret123", "Secret123"));

        assertThat(user.getId()).isNotNull();
        assertThat(user.getFullName()).isEqualTo("Ada Obi");
        assertThat(user.getEmail()).isEqualTo("ada@example.com");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getPassword()).isNotEqualTo("Secret123");
        assertThat(passwordEncoder.matches("Secret123", user.getPassword())).isTrue();
    }

    @Test
    void registerRejectsMismatchedPasswords() {
        assertThatThrownBy(() -> memberService.register(form("Ada", "ada@example.com", "Secret123", "Different1")))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("do not match");
        assertThat(userRepository.existsByEmailIgnoreCase("ada@example.com")).isFalse();
    }

    @Test
    void registerRejectsDuplicateEmailIgnoringCase() {
        memberService.register(form("Ada", "ada@example.com", "Secret123", "Secret123"));

        assertThatThrownBy(() -> memberService.register(form("Ada Two", "ADA@EXAMPLE.COM", "Secret123", "Secret123")))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void registerNeverCreatesAnAdmin() {
        AppUser user = memberService.register(form("Ada", "ada@example.com", "Secret123", "Secret123"));

        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void seededAdminExists() {
        AppUser admin = memberService.requireByEmail("admin@library.com");

        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(passwordEncoder.matches("Admin@123", admin.getPassword())).isTrue();
    }

    @Test
    void listMembersExcludesAdminsAndSortsByName() {
        memberService.createUser("Zed Last", "zed@example.com", "Secret123", Role.USER);
        memberService.createUser("Amy First", "amy@example.com", "Secret123", Role.USER);

        assertThat(memberService.listMembers()).extracting(AppUser::getFullName)
                .containsSubsequence("Amy First", "Zed Last")
                .doesNotContain("Library Admin");
    }

    @Test
    void toggleEnabledFlipsTheFlagBothWays() {
        AppUser member = newMember();

        assertThat(memberService.toggleEnabled(member.getId()).isEnabled()).isFalse();
        assertThat(memberService.toggleEnabled(member.getId()).isEnabled()).isTrue();
    }

    @Test
    void adminAccountCannotBeToggled() {
        AppUser admin = memberService.requireByEmail("admin@library.com");

        assertThatThrownBy(() -> memberService.toggleEnabled(admin.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("Only member accounts");
    }

    @Test
    void lookupsFailForUnknownAccounts() {
        assertThatThrownBy(() -> memberService.get(-1L))
                .isInstanceOf(LibraryException.class).hasMessageContaining("Member not found");
        assertThatThrownBy(() -> memberService.requireByEmail("nobody@example.com"))
                .isInstanceOf(LibraryException.class).hasMessageContaining("Account not found");
    }
}
