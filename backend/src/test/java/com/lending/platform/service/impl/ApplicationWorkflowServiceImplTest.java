package com.lending.platform.service.impl;

import com.lending.platform.dto.request.ApplicationStatusTransitionRequest;
import com.lending.platform.dto.response.ApplicationStatusHistoryResponse;
import com.lending.platform.dto.response.LoanApplicationResponse;
import com.lending.platform.entity.*;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.ApplicationStatusHistoryMapper;
import com.lending.platform.mapper.LoanApplicationMapper;
import com.lending.platform.repository.ApplicationStatusHistoryRepository;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import com.lending.platform.service.ApplicationWorkflowRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationWorkflowServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private ApplicationStatusHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    private ApplicationWorkflowRules workflowRules;
    private LoanApplicationMapper loanApplicationMapper;
    private ApplicationStatusHistoryMapper historyMapper;
    private ApplicationWorkflowServiceImpl workflowService;

    @BeforeEach
    void setUp() {
        workflowRules = new ApplicationWorkflowRules();
        loanApplicationMapper = new LoanApplicationMapper();
        historyMapper = new ApplicationStatusHistoryMapper();

        workflowService = new ApplicationWorkflowServiceImpl(
                loanApplicationRepository,
                historyRepository,
                userRepository,
                workflowRules,
                loanApplicationMapper,
                historyMapper
        );
    }

    @Test
    void transitionStatus_shouldMoveDraftToSubmitted() {

        LoanApplication application =
                createApplication(ApplicationStatus.DRAFT);

        User user = createUser();

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(user));

        when(loanApplicationRepository.save(application))
                .thenReturn(application);

        when(historyRepository.save(any(ApplicationStatusHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse response =
                workflowService.transitionStatus(
                        10L,
                        new ApplicationStatusTransitionRequest(
                                ApplicationStatus.SUBMITTED,
                                "Ready for processing"
                        ),
                        "admin@lending.local"
                );

        assertEquals(
                ApplicationStatus.SUBMITTED,
                response.status()
        );

        assertNotNull(response.submittedAt());

        verify(loanApplicationRepository)
                .save(application);

        verify(historyRepository)
                .save(any(ApplicationStatusHistory.class));
    }

    @Test
    void transitionStatus_shouldCreateHistoryRecord() {

        LoanApplication application =
                createApplication(ApplicationStatus.DRAFT);

        User user = createUser();

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(user));

        when(loanApplicationRepository.save(application))
                .thenReturn(application);

        when(historyRepository.save(any(ApplicationStatusHistory.class)))
                .thenAnswer(invocation -> {
                    ApplicationStatusHistory history =
                            invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            history,
                            "id",
                            100L
                    );

                    history.onCreate();
                    return history;
                });

        workflowService.transitionStatus(
                10L,
                new ApplicationStatusTransitionRequest(
                        ApplicationStatus.SUBMITTED,
                        "Submitted by loan officer"
                ),
                "admin@lending.local"
        );

        var captor =
                org.mockito.ArgumentCaptor.forClass(
                        ApplicationStatusHistory.class
                );

        verify(historyRepository).save(captor.capture());

        ApplicationStatusHistory history =
                captor.getValue();

        assertEquals(
                ApplicationStatus.DRAFT,
                history.getFromStatus()
        );

        assertEquals(
                ApplicationStatus.SUBMITTED,
                history.getToStatus()
        );

        assertEquals(
                "Submitted by loan officer",
                history.getComment()
        );

        assertEquals(
                user,
                history.getChangedByUser()
        );

        assertEquals(
                application,
                history.getApplication()
        );
    }

    @Test
    void transitionStatus_shouldRejectInvalidTransition() {

        LoanApplication application =
                createApplication(ApplicationStatus.DRAFT);

        User user = createUser();

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(user));

        assertThrows(
                ResourceConflictException.class,
                () -> workflowService.transitionStatus(
                        10L,
                        new ApplicationStatusTransitionRequest(
                                ApplicationStatus.UNDERWRITING,
                                null
                        ),
                        "admin@lending.local"
                )
        );

        verify(loanApplicationRepository, never())
                .save(any());

        verify(historyRepository, never())
                .save(any());
    }

    @Test
    void transitionStatus_shouldSetDecisionTimestampForApproved() {

        LoanApplication application =
                createApplication(
                        ApplicationStatus.CREDIT_REVIEW
                );

        User user = createUser();

        stubTransitionDependencies(
                application,
                user
        );

        LoanApplicationResponse response =
                workflowService.transitionStatus(
                        10L,
                        new ApplicationStatusTransitionRequest(
                                ApplicationStatus.APPROVED,
                                "Credit approved"
                        ),
                        "admin@lending.local"
                );

        assertEquals(
                ApplicationStatus.APPROVED,
                response.status()
        );

        assertNotNull(response.decisionAt());
    }

    @Test
    void transitionStatus_shouldSetDecisionTimestampForDeclined() {

        LoanApplication application =
                createApplication(
                        ApplicationStatus.CREDIT_REVIEW
                );

        User user = createUser();

        stubTransitionDependencies(
                application,
                user
        );

        LoanApplicationResponse response =
                workflowService.transitionStatus(
                        10L,
                        new ApplicationStatusTransitionRequest(
                                ApplicationStatus.DECLINED,
                                "Credit declined"
                        ),
                        "admin@lending.local"
                );

        assertEquals(
                ApplicationStatus.DECLINED,
                response.status()
        );

        assertNotNull(response.decisionAt());
    }

    @Test
    void transitionStatus_shouldSetFundedTimestamp() {

        LoanApplication application =
                createApplication(ApplicationStatus.CLOSING);

        User user = createUser();

        stubTransitionDependencies(
                application,
                user
        );

        LoanApplicationResponse response =
                workflowService.transitionStatus(
                        10L,
                        new ApplicationStatusTransitionRequest(
                                ApplicationStatus.FUNDED,
                                "Loan funded"
                        ),
                        "admin@lending.local"
                );

        assertEquals(
                ApplicationStatus.FUNDED,
                response.status()
        );

        assertNotNull(response.fundedAt());
    }

    @Test
    void transitionStatus_shouldThrowWhenApplicationMissing() {

        when(loanApplicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> workflowService.transitionStatus(
                        999L,
                        new ApplicationStatusTransitionRequest(
                                ApplicationStatus.SUBMITTED,
                                null
                        ),
                        "admin@lending.local"
                )
        );

        verify(userRepository, never())
                .findByEmailIgnoreCase(anyString());
    }

    @Test
    void transitionStatus_shouldThrowWhenAuthenticatedUserMissing() {

        LoanApplication application =
                createApplication(ApplicationStatus.DRAFT);

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findByEmailIgnoreCase(
                "missing@lending.local"
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> workflowService.transitionStatus(
                        10L,
                        new ApplicationStatusTransitionRequest(
                                ApplicationStatus.SUBMITTED,
                                null
                        ),
                        "missing@lending.local"
                )
        );

        verify(loanApplicationRepository, never())
                .save(any());

        verify(historyRepository, never())
                .save(any());
    }

    @Test
    void getStatusHistory_shouldReturnOrderedHistory() {

        LoanApplication application =
                createApplication(
                        ApplicationStatus.DOCUMENT_COLLECTION
                );

        User user = createUser();

        ApplicationStatusHistory first =
                createHistory(
                        100L,
                        application,
                        user,
                        ApplicationStatus.DRAFT,
                        ApplicationStatus.SUBMITTED,
                        LocalDateTime.of(
                                2026, 8, 24, 10, 0
                        )
                );

        ApplicationStatusHistory second =
                createHistory(
                        101L,
                        application,
                        user,
                        ApplicationStatus.SUBMITTED,
                        ApplicationStatus.DOCUMENT_COLLECTION,
                        LocalDateTime.of(
                                2026, 8, 24, 11, 0
                        )
                );

        when(loanApplicationRepository.existsById(10L))
                .thenReturn(true);

        when(historyRepository
                .findAllByApplicationIdOrderByChangedAtAsc(10L))
                .thenReturn(List.of(first, second));

        List<ApplicationStatusHistoryResponse> history =
                workflowService.getStatusHistory(10L);

        assertEquals(2, history.size());

        assertEquals(
                ApplicationStatus.SUBMITTED,
                history.get(0).toStatus()
        );

        assertEquals(
                ApplicationStatus.DOCUMENT_COLLECTION,
                history.get(1).toStatus()
        );

        assertEquals(
                "System Administrator",
                history.get(0).changedByUserName()
        );
    }

    @Test
    void getStatusHistory_shouldThrowWhenApplicationMissing() {

        when(loanApplicationRepository.existsById(999L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> workflowService.getStatusHistory(999L)
        );

        verify(historyRepository, never())
                .findAllByApplicationIdOrderByChangedAtAsc(
                        anyLong()
                );
    }

    private void stubTransitionDependencies(
            LoanApplication application,
            User user
    ) {

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(user));

        when(loanApplicationRepository.save(application))
                .thenReturn(application);

        when(historyRepository.save(
                any(ApplicationStatusHistory.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );
    }

    private LoanApplication createApplication(
            ApplicationStatus status
    ) {

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
                "APP-2026-TEST0001"
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
        application.setStatus(status);

        application.onCreate();
        application.setStatus(status);

        return application;
    }

    private User createUser() {

        User user = new User();

        ReflectionTestUtils.setField(
                user,
                "id",
                1L
        );

        user.setFirstName("System");
        user.setLastName("Administrator");
        user.setEmail("admin@lending.local");
        user.setEnabled(true);

        return user;
    }

    private ApplicationStatusHistory createHistory(
            Long id,
            LoanApplication application,
            User user,
            ApplicationStatus fromStatus,
            ApplicationStatus toStatus,
            LocalDateTime changedAt
    ) {

        ApplicationStatusHistory history =
                new ApplicationStatusHistory();

        ReflectionTestUtils.setField(
                history,
                "id",
                id
        );

        history.setApplication(application);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedByUser(user);
        history.setComment("Workflow update");

        ReflectionTestUtils.setField(
                history,
                "changedAt",
                changedAt
        );

        return history;
    }
}
