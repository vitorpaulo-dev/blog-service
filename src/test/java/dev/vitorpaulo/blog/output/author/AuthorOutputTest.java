package dev.vitorpaulo.blog.output.author;

import com.clerk.backend_api.Clerk;
import com.clerk.backend_api.models.components.OrganizationMembership;
import com.clerk.backend_api.models.components.OrganizationMemberships;
import com.clerk.backend_api.models.components.User;
import com.clerk.backend_api.models.operations.GetUserResponse;
import com.clerk.backend_api.models.operations.ListOrganizationMembershipsRequest;
import com.clerk.backend_api.models.operations.ListOrganizationMembershipsResponse;
import dev.vitorpaulo.blog.domain.AuthorEntity;
import dev.vitorpaulo.blog.config.security.AuthorRoleMapper;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.output.mapper.AuthorOutputMapper;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthorOutputTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Clerk clerk;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorOutputMapper authorOutputMapper;

    @InjectMocks
    private AuthorOutput authorOutput;

    private final UUID authorId = UUID.randomUUID();

    private final Jwt adminJwt = new Jwt("raw", Instant.now(), Instant.now().plusSeconds(3600),
        Map.of("alg", "RS256"), Map.of("sub", "user_sub", "org_role", "org:admin"));

    private final User user = mock(User.class);

    private final GetUserResponse getUserResponse = mock(GetUserResponse.class);

    private final AuthorEntity authorEntity = AuthorEntity.builder()
        .id(authorId)
        .subjectId("user_sub")
        .build();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authorOutput, "organizationId", "org_1");
        when(clerk.users().get(anyString())).thenReturn(getUserResponse);
        when(getUserResponse.user()).thenReturn(Optional.of(user));
        when(user.firstName()).thenReturn(Optional.of("Vitor"));
        when(user.lastName()).thenReturn(Optional.empty());
        when(user.imageUrl()).thenReturn(Optional.empty());
        when(user.id()).thenReturn("user_sub");

        when(authorRepository.findBySubjectId("user_sub")).thenReturn(Optional.of(authorEntity));
        when(authorRepository.countBySlugAndIdNot(anyString(), any())).thenReturn(0L);
    }

    @Test
    void getAuthor_membershipResolved_usesClerkRole() {
        stubMembership(Optional.of(stubMembershipResponse(List.of(membershipWithRole("org:member")))));

        var model = new AuthorModel(authorId, "Vitor", null, null, null, "org:member");
        when(authorOutputMapper.toModel(authorEntity, "org:member")).thenReturn(model);

        assertEquals(model, authorOutput.getAuthor(adminJwt));
    }

    @Test
    void getAuthor_membershipLookupFails_fallsBackToJwtOrgRole() {
        when(clerk.organizationMemberships().list(any(ListOrganizationMembershipsRequest.class)))
            .thenThrow(new RuntimeException("clerk down"));

        var model = new AuthorModel(authorId, "Vitor", null, null, null, "org:admin");
        when(authorOutputMapper.toModel(authorEntity, "org:admin")).thenReturn(model);

        assertEquals(model, authorOutput.getAuthor(adminJwt));
    }

    @Test
    void getAuthor_noMembershipAndNoClaim_defaultsToMemberRole() {
        stubMembership(Optional.of(stubMembershipResponse(List.of())));

        var model = new AuthorModel(authorId, "Vitor", null, null, null, AuthorRoleMapper.MEMBER_CLERK_ROLE);
        when(authorOutputMapper.toModel(authorEntity, AuthorRoleMapper.MEMBER_CLERK_ROLE)).thenReturn(model);

        var rolelessJwt = new Jwt("raw", Instant.now(), Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"), Map.of("sub", "user_sub"));

        assertEquals(model, authorOutput.getAuthor(rolelessJwt));
    }

    private void stubMembership(Optional<OrganizationMemberships> memberships) {
        var response = mock(ListOrganizationMembershipsResponse.class);
        when(response.organizationMemberships()).thenReturn(memberships);
        when(clerk.organizationMemberships().list(any(ListOrganizationMembershipsRequest.class)))
            .thenReturn(response);
    }

    private OrganizationMemberships stubMembershipResponse(List<OrganizationMembership> data) {
        var memberships = mock(OrganizationMemberships.class);
        when(memberships.data()).thenReturn(data);
        return memberships;
    }

    private OrganizationMembership membershipWithRole(String role) {
        var membership = mock(OrganizationMembership.class);
        when(membership.role()).thenReturn(role);
        return membership;
    }
}
