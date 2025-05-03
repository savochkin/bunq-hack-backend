package nl.bunq.hackathon.app.service;

import nl.bunq.hackathon.app.model.Bill;
import nl.bunq.hackathon.app.port.out.BankPort;
import nl.bunq.hackathon.app.port.out.BillRepository;
import nl.bunq.hackathon.app.port.out.OrderMatcher;
import nl.bunq.hackathon.app.port.out.ReceiptParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private ReceiptParser receiptParser;

    @Mock
    private OrderMatcher orderMatcher;

    @Mock
    private BankPort bankPort;

    private BillServiceImpl billService;

    @BeforeEach
    void setUp() {
        billService = new BillServiceImpl(billRepository, receiptParser, orderMatcher, bankPort);
    }

    @Test
    void createBill_shouldCreateAndSaveBillWithCorrectName() {
        // Given
        String billName = "Test Bill";
        Bill savedBill = Bill.builder()
                .id(UUID.randomUUID())
                .name(billName)
                .createdAt(LocalDateTime.now())
                .shareCode("TESTCODE")
                .build();

        when(billRepository.save(any(Bill.class))).thenReturn(savedBill);

        // When
        Bill result = billService.createBill(billName);

        // Then
        ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
        verify(billRepository).save(billCaptor.capture());

        Bill capturedBill = billCaptor.getValue();
        assertThat(capturedBill).isNotNull();
        assertThat(capturedBill.getName()).isEqualTo(billName);
        assertThat(capturedBill.getId()).isNotNull();
        assertThat(capturedBill.getCreatedAt()).isNotNull();
        assertThat(capturedBill.getShareCode()).isNotNull().hasSize(8);
        assertThat(capturedBill.getReceipts()).isNotNull().isEmpty();
        assertThat(capturedBill.getPaymentTabs()).isNotNull().isEmpty();

        // Verify the returned bill matches what was saved
        assertThat(result).isEqualTo(savedBill);
    }
}