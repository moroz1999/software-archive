package com.archive.backend.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class UserValuesTest {

    @Test
    void aUserNameIsRequired() {
        assertInvalid(() -> new UserName(" "), "user.name-required");
    }

    @Test
    void aUserNameIsAtMost64CharactersLong() {
        assertThat(new UserName("a".repeat(64)).value()).hasSize(64);
        assertInvalid(() -> new UserName("a".repeat(65)), "user.name-too-long");
    }

    @Test
    void anEmailIsRequired() {
        assertInvalid(() -> new Email(""), "user.email-required");
    }

    @Test
    void anEmailNeedsAnAddressAndADomain() {
        assertInvalid(() -> new Email("zed"), "user.email-invalid");
        assertInvalid(() -> new Email("zed@"), "user.email-invalid");
        assertInvalid(() -> new Email("@example.com"), "user.email-invalid");
        assertInvalid(() -> new Email("zed @example.com"), "user.email-invalid");
    }

    @Test
    void anEmailIsAtMost254CharactersLong() {
        assertInvalid(() -> new Email("a".repeat(243) + "@example.com"), "user.email-too-long");
    }

    @Test
    void anEmailIsKeptInLowerCaseWithoutSurroundingSpaces() {
        assertThat(new Email(" Zed@Example.COM ")).isEqualTo(new Email("zed@example.com"));
    }

    @Test
    void aPasswordIsAtLeast8CharactersLong() {
        assertThat(new Password("12345678").value()).isEqualTo("12345678");
        assertInvalid(() -> new Password("1234567"), "user.password-too-short");
    }

    @Test
    void aPasswordIsAtMost128CharactersLong() {
        assertInvalid(() -> new Password("a".repeat(129)), "user.password-too-long");
    }

    @Test
    void aPasswordIsNeverPrinted() {
        assertThat(new Password("secret-password")).hasToString("Password[***]");
    }

    @Test
    void aLoginIsRequired() {
        assertInvalid(() -> new Login(" "), "user.login-required");
    }

    @Test
    void aConfirmationTokenIsRequired() {
        assertInvalid(() -> new ConfirmationToken(""), "user.confirmation-token-required");
    }

    @Test
    void generatedConfirmationTokensAreUrlSafeAndUnique() {
        ConfirmationToken first = ConfirmationToken.generate();

        assertThat(first.value()).matches("[A-Za-z0-9_-]{43,}");
        assertThat(ConfirmationToken.generate()).isNotEqualTo(first);
    }

    @Test
    void theDigestOfATokenIsStableAndHidesIt() {
        ConfirmationToken token = new ConfirmationToken("secret");

        assertThat(token.digest()).isEqualTo(new ConfirmationToken("secret").digest());
        assertThat(token.digest()).isNotEqualTo(new ConfirmationToken("other").digest());
        assertThat(token.digest().value()).matches("[0-9a-f]{64}").doesNotContain("secret");
    }

    @Test
    void userGroupsAreKnownByTheirCodes() {
        for (UserGroup group : UserGroup.values()) {
            assertThat(UserGroup.ofCode(group.code())).isEqualTo(group);
        }
    }

    @Test
    void administratorsAndModeratorsAreTheGroupsGivenOnPurpose() {
        assertThat(GrantedGroups.ofCodes(List.of("admin", "moderator")))
                .isEqualTo(new GrantedGroups(Set.of(UserGroup.ADMIN, UserGroup.MODERATOR)));
        assertThat(GrantedGroups.ofCodes(List.of())).isEqualTo(new GrantedGroups(Set.of()));
    }

    @Test
    void membersAndPublicVisitorsAreNotGivenOnPurpose() {
        assertInvalid(() -> GrantedGroups.ofCodes(List.of("member")), "user.group-invalid");
        assertInvalid(() -> GrantedGroups.ofCodes(List.of("public-visitor")), "user.group-invalid");
        assertInvalid(() -> new GrantedGroups(Set.of(UserGroup.MEMBER)), "user.group-invalid");
    }

    @Test
    void anUnknownGroupIsInvalid() {
        assertInvalid(() -> GrantedGroups.ofCodes(List.of("owner")), "user.group-invalid");
    }

    @Test
    void aSearchOfUsersIsWhatWasTypedWithoutSurroundingSpaces() {
        assertThat(UserSearch.of("  zed ")).isEqualTo(new UserSearch("zed"));
        assertThat(UserSearch.of("   ")).isNull();
        assertThat(UserSearch.of(null)).isNull();
    }

    @Test
    void lettersAreWrittenInRussianOrEnglish() {
        assertThat(InterfaceLanguage.ofCode("ru")).isEqualTo(InterfaceLanguage.RU);
        assertThat(InterfaceLanguage.ofCode("en")).isEqualTo(InterfaceLanguage.EN);
        assertThat(InterfaceLanguage.ofCode("de")).isEqualTo(InterfaceLanguage.EN);
    }

    private static void assertInvalid(ThrowingCallable creation, String code) {
        assertThatThrownBy(creation)
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo(code));
    }
}
