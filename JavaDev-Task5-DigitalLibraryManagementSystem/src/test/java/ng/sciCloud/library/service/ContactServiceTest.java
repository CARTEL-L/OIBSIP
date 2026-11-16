package ng.sciCloud.library.service;

import ng.sciCloud.library.IntegrationTestBase;
import ng.sciCloud.library.dto.ContactForm;
import ng.sciCloud.library.exception.LibraryException;
import ng.sciCloud.library.model.AppUser;
import ng.sciCloud.library.model.ContactMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ng.sciCloud.library.repository.ContactMessageRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContactServiceTest extends IntegrationTestBase {

    @Autowired
    private ContactService contactService;
    @Autowired
    private ContactMessageRepository messageRepository;

    private ContactForm form(String subject, String message) {
        ContactForm form = new ContactForm();
        form.setSubject(subject);
        form.setMessage(message);
        return form;
    }

    @Test
    void submitStoresMessageWithSenderDetails() {
        AppUser member = newMember();

        ContactMessage saved = contactService.submit(member, form("  Opening hours ", " When do you open? "));

        assertThat(saved.getId()).isNotNull();
        assertThat(messageRepository.findById(saved.getId())).isPresent();
        assertThat(saved.getName()).isEqualTo(member.getFullName());
        assertThat(saved.getEmail()).isEqualTo(member.getEmail());
        assertThat(saved.getSubject()).isEqualTo("Opening hours");
        assertThat(saved.getMessage()).isEqualTo("When do you open?");
        assertThat(saved.isResolved()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findAllReturnsNewestFirst() {
        AppUser member = newMember();
        messageRepository.save(message(member, "Older", LocalDateTime.now().minusHours(2)));
        messageRepository.save(message(member, "Newer", LocalDateTime.now()));

        assertThat(contactService.findAll()).extracting(ContactMessage::getSubject)
                .containsExactly("Newer", "Older");
    }

    private ContactMessage message(AppUser member, String subject, LocalDateTime createdAt) {
        ContactMessage message = new ContactMessage();
        message.setUser(member);
        message.setName(member.getFullName());
        message.setEmail(member.getEmail());
        message.setSubject(subject);
        message.setMessage("body");
        message.setCreatedAt(createdAt);
        return message;
    }

    @Test
    void markResolvedUpdatesTheMessageAndOpenCount() {
        AppUser member = newMember();
        long openBefore = messageRepository.countByResolvedFalse();
        ContactMessage saved = contactService.submit(member, form("Help", "Please help"));
        assertThat(messageRepository.countByResolvedFalse()).isEqualTo(openBefore + 1);

        contactService.markResolved(saved.getId());

        assertThat(saved.isResolved()).isTrue();
        assertThat(messageRepository.countByResolvedFalse()).isEqualTo(openBefore);
    }

    @Test
    void markResolvedFailsForUnknownMessage() {
        assertThatThrownBy(() -> contactService.markResolved(-1L))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("Message not found");
    }
}
