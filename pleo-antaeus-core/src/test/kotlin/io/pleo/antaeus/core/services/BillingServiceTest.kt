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

    private val invoiceList = listOf(
            Invoice(1, 1, Money(BigDecimal.valueOf(10), Currency.EUR), InvoiceStatus.PENDING),
            Invoice(2, 2, Money(BigDecimal.valueOf(22), Currency.EUR), InvoiceStatus.PENDING),
            Invoice(3, 3, Money(BigDecimal.valueOf(25), Currency.EUR), InvoiceStatus.PENDING)
    )
    private val invoicePaidList = invoiceList.map { it.copy(status = InvoiceStatus.PAID) }
    private val invoiceErrorList = invoiceList.map { it.copy(status = InvoiceStatus.ERROR) }

    @Test
    fun `will return invoice with PAID status when payment provider processes charge`() {
        val invoice = invoiceList.first()
        val invoicePaid = invoice.copy(status = InvoiceStatus.PAID)

        val dal = mockk<AntaeusDal> {
            every { updateInvoiceStatus(invoice.id, InvoiceStatus.PAID) } returns invoicePaid
        }
        val invoiceService = mockk<InvoiceService> {
            every { fetch(invoice.id) } returns invoice
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

    @Test
    fun `will return invoice with ERROR status when payment provider does not process charge`() {
        val invoice = invoiceList.first()
        val invoiceError = invoice.copy(status = InvoiceStatus.ERROR)

        val dal = mockk<AntaeusDal>{
            every { updateInvoiceStatus(invoice.id, InvoiceStatus.ERROR) } returns invoiceError
        }

        val invoiceService = mockk<InvoiceService> {
            every { fetch(invoice.id) } returns invoice
        }

        val paymentProvider = mockk<PaymentProvider> {
            every { charge(invoice) } returns false
        }

        val billingService = BillingService(
                dal,
                paymentProvider,
                invoiceService
        )

        val updatedInvoice = billingService.charge(1)
        if (updatedInvoice != null) {
            assertEquals(InvoiceStatus.ERROR, updatedInvoice.status)
        }
    }

    @Test
    fun `will return invoice list with PAID status when payment provider processes charge`() {
        val dal = mockk<AntaeusDal> {
            every { updateInvoiceStatus(any(), InvoiceStatus.PAID) } answers {
                invoicePaidList[firstArg<Int>() - 1]
            }
        }
        val invoiceService = mockk<InvoiceService> {
            every { fetchPendingInvoices() } returns invoiceList
        }
        val paymentProvider = mockk<PaymentProvider> {
            every { charge(any()) } returns true
        }

        val billingService = BillingService(
                dal,
                paymentProvider,
                invoiceService
        )

        val updatedInvoices = billingService.chargeAll()
        assertEquals(invoicePaidList, updatedInvoices)
    }

    @Test
    fun `will return invoice list with ERROR status when payment provider does not process charge`() {
        val dal = mockk<AntaeusDal> {
            every { updateInvoiceStatus(any(), InvoiceStatus.ERROR) } answers {
                invoiceErrorList[firstArg<Int>() - 1]
            }
        }
        val invoiceService = mockk<InvoiceService> {
            every { fetchPendingInvoices() } returns invoiceList
        }
        val paymentProvider = mockk<PaymentProvider> {
            every { charge(any()) } returns false
        }

        val billingService = BillingService(
                dal,
                paymentProvider,
                invoiceService
        )

        val updatedInvoices = billingService.chargeAll()
        assertEquals(invoiceErrorList, updatedInvoices)
    }

    @Test
    fun `will return invoice list with one ERROR status`() {
        val dal = mockk<AntaeusDal> {
            every { updateInvoiceStatus(any(), InvoiceStatus.ERROR) } answers {invoiceErrorList[firstArg<Int>() - 1] }
            every { updateInvoiceStatus(any(), InvoiceStatus.PAID) } answers { invoicePaidList[firstArg<Int>() - 1] }
        }
        val invoiceService = mockk<InvoiceService> {
            every { fetchPendingInvoices() } returns invoiceList
        }
        val paymentProvider = mockk<PaymentProvider> {
            every { charge(any()) } answers {
                firstArg<Invoice>().id != 1
            }
        }

        val billingService = BillingService(
                dal,
                paymentProvider,
                invoiceService
        )

        val updatedInvoices = billingService.chargeAll()
        val expectedInvoices = invoicePaidList.map {
            if (it.id == 1) {
                it.copy(status = InvoiceStatus.ERROR)
            } else{
                it.copy()
            }
        }
        assertEquals(expectedInvoices, updatedInvoices)
    }
}