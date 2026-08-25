package com.lending.platform.service.impl;

import com.lending.platform.dto.request.ApplicationAssignmentRequest;
import com.lending.platform.dto.response.LoanApplicationResponse;
import com.lending.platform.entity.*;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.LoanApplicationMapper;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationAssignmentServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private UserRepository userRepository;

    private LoanApplicationMapper loanApplicationMapper;
    private ApplicationAssignmentServiceImpl assignmentService;

    @BeforeEach
    void setUp() {
        loanApplicationMapper = new LoanApplicationMapper();

        assignmentService = new ApplicationAssignmentServiceImpl(
                loanApplicationRepository,
                userRepository,
                loanApplicationMapper
        );
    }

    @Test
    void assignLoanOfficer_shouldAssignValidUser() {

        LoanApplication application = createApplication();
        User loanOfficer = createUser(
                2L,
                "Loan",
                "Officer",
                "loan.officer@lending.local",
                "LOAN_OFFICER",
                true
        );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(loanOfficer));

        when(loanApplicationRepository.save(application))
                .thenReturn(application);

        LoanApplicationResponse response =
                assignmentService.assignLoanOfficer(
                        10L,
                        new ApplicationAssignmentRequest(2L)
                );

        assertEquals(2L, response.assignedLoanOfficerId());
        assertEquals(
                "Loan Officer",
                response.assignedLoanOfficerName()
        );

        verify(loanApplicationRepository).save(application);
    }

    @Test
    void assignLoanOfficer_shouldRejectUserWithoutRequiredRole() {

        LoanApplication application = createApplication();

        User underwriter = createUser(
                3L,
                "Uma",
                "Underwriter",
                "uma@lending.local",
                "UNDERWRITER",
                true
        );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(underwriter));

        assertThrows(
                ResourceConflictException.class,
                () -> assignmentService.assignLoanOfficer(
                        10L,
                        new ApplicationAssignmentRequest(3L)
                )
        );

        verify(loanApplicationRepository, never())
                .save(any());
    }

    @Test
    void assignLoanOfficer_shouldRejectDisabledUser() {

        LoanApplication application = createApplication();

        User disabledLoanOfficer = createUser(
                2L,
                "Loan",
                "Officer",
                "loan.officer@lending.local",
                "LOAN_OFFICER",
                false
        );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(disabledLoanOfficer));

        assertThrows(
                ResourceConflictException.class,
                () -> assignmentService.assignLoanOfficer(
                        10L,
                        new ApplicationAssignmentRequest(2L)
                )
        );

        verify(loanApplicationRepository, never())
                .save(any());
    }

    @Test
    void assignUnderwriter_shouldAssignValidUser() {

        LoanApplication application = createApplication();

        User underwriter = createUser(
                3L,
                "Uma",
                "Underwriter",
                "uma@lending.local",
                "UNDERWRITER",
                true
        );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(underwriter));

        when(loanApplicationRepository.save(application))
                .thenReturn(application);

        LoanApplicationResponse response =
                assignmentService.assignUnderwriter(
                        10L,
                        new ApplicationAssignmentRequest(3L)
                );

        assertEquals(3L, response.assignedUnderwriterId());
        assertEquals(
                "Uma Underwriter",
                response.assignedUnderwriterName()
        );

        verify(loanApplicationRepository).save(application);
    }

    @Test
    void assignUnderwriter_shouldRejectUserWithoutRequiredRole() {

        LoanApplication application = createApplication();

        User loanOfficer = createUser(
                2L,
                "Loan",
                "Officer",
                "loan.officer@lending.local",
                "LOAN_OFFICER",
                true
        );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(loanOfficer));

        assertThrows(
                ResourceConflictException.class,
                () -> assignmentService.assignUnderwriter(
                        10L,
                        new ApplicationAssignmentRequest(2L)
                )
        );

        verify(loanApplicationRepository, never())
                .save(any());
    }

    @Test
    void assignUnderwriter_shouldRejectDisabledUser() {

        LoanApplication application = createApplication();

        User disabledUnderwriter = createUser(
                3L,
                "Uma",
                "Underwriter",
                "uma@lending.local",
                "UNDERWRITER",
                false
        );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(disabledUnderwriter));

        assertThrows(
                ResourceConflictException.class,
                () -> assignmentService.assignUnderwriter(
                        10L,
                        new ApplicationAssignmentRequest(3L)
                )
        );

        verify(loanApplicationRepository, never())
                .save(any());
    }

    @Test
    void assign_shouldThrowWhenApplicationMissing() {

        when(loanApplicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> assignmentService.assignLoanOfficer(
                        999L,
                        new ApplicationAssignmentRequest(2L)
                )
        );

        verify(userRepository, never())
                .findById(anyLong());
    }

    @Test
    void assign_shouldThrowWhenUserMissing() {

        LoanApplication application = createApplication();

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> assignmentService.assignLoanOfficer(
                        10L,
                        new ApplicationAssignmentRequest(999L)
                )
        );

        verify(loanApplicationRepository, never())
                .save(any());
    }

    @Test
    void unassignLoanOfficer_shouldClearAssignment() {

        LoanApplication application = createApplication();

        application.setAssignedLoanOfficer(
                createUser(
                        2L,
                        "Loan",
                        "Officer",
                        "loan.officer@lending.local",
                        "LOAN_OFFICER",
                        true
                )
        );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(loanApplicationRepository.save(application))
                .thenReturn(application);

        LoanApplicationResponse response =
                assignmentService.unassignLoanOfficer(10L);

        assertNull(response.assignedLoanOfficerId());
        assertNull(response.assignedLoanOfficerName());

        verify(loanApplicationRepository).save(application);
    }

    @Test
    void unassignUnderwriter_shouldClearAssignment() {

        LoanApplication application = createApplication();

        application.setAssignedUnderwriter(
                createUser(
                        3L,
                        "Uma",
                        "Underwriter",
                        "uma@lending.local",
                        "UNDERWRITER",
                        true
                )
        );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(loanApplicationRepository.save(application))
                .thenReturn(application);

        LoanApplicationResponse response =
                assignmentService.unassignUnderwriter(10L);

        assertNull(response.assignedUnderwriterId());
        assertNull(response.assignedUnderwriterName());

        verify(loanApplicationRepository).save(application);
    }

    private LoanApplication createApplication() {

        Business business = new Business();

        ReflectionTestUtils.setField(
                business,
                "id",
                1L
        );

        business.setLegalName(
                "Atlanta Logistics LLC"
        );

        LoanApplication application =
                new LoanApplication();

        ReflectionTestUtils.setField(
                application,
                "id",
                10L
        );

        application.setBusiness(business);

        application.setApplicationNumber(
                "APP-2026-ASSIGN001"
        );

        application.setLoanProduct(
                LoanProduct.SBA_7A
        );

        application.setLoanPurpose(
                LoanPurpose.EQUIPMENT_PURCHASE
        );

        application.setRequestedAmount(
                new BigDecimal("350000.00")
        );

        application.setRequestedTermMonths(120);
        application.setStatus(ApplicationStatus.SUBMITTED);

        application.onCreate();
        application.setStatus(ApplicationStatus.SUBMITTED);

        return application;
    }

    private User createUser(
            Long id,
            String firstName,
            String lastName,
            String email,
            String roleName,
            boolean enabled
    ) {

        Role role = new Role();
        role.setName(roleName);

        User user = new User();

        ReflectionTestUtils.setField(
                user,
                "id",
                id
        );

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setEnabled(enabled);
        user.setRoles(Set.of(role));

        return user;
    }
}