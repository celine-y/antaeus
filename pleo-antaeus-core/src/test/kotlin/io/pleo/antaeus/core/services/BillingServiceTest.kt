package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BillingServiceTest {

    @Test
    fun `will return true when payment provider processes charge`() {
        val invoice = Invoice(1, 1, Money(BigDecimal.TEN, Currency.EUR), InvoiceStatus.PENDING)
        val invoicePaid = invoice.copy(status = InvoiceStatus.PAID)

        val dal = mockk<AntaeusDal> {
            every { updateInvoiceStatus(1, InvoiceStatus.PAID) } returns invoicePaid
        }
        val invoiceService = mockk<InvoiceService> {
            every { fetch(1) } returns invoice
        }
        val paymentProvider = mockk<PaymentProvider> {
            every { charge(invoice) } returns true
        }

        val billingService = BillingService(
                dal,
                paymentProvider,
                invoiceService
        )

        val updatedInvoice = billingService.charge(1)
        if (updatedInvoice != null) {
            assertEquals(InvoiceStatus.PAID, updatedInvoice.status)
        }
    }
}