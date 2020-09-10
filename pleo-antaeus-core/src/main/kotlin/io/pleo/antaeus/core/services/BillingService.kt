package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import kotlin.math.log

private val logger = KotlinLogging.logger {}

class BillingService(
        private val dal: AntaeusDal,
        private val paymentProvider: PaymentProvider,
        private val invoiceService: InvoiceService
) {
    fun chargeAll(): List<Invoice?> {
        val chargedInvoices = arrayListOf<Invoice>()
        val invoices = invoiceService.fetchPendingInvoices()

        for (invoice in invoices) {
            val chargedInvoice = charge(invoice)
            if (chargedInvoice != null) {
                chargedInvoices.add(chargedInvoice)
            }
        }
        return chargedInvoices
    }

    fun charge(id: Int): Invoice? {
        val invoice = invoiceService.fetch(id)

        if (invoice.status == InvoiceStatus.PENDING){
            return charge(invoice)
        }
        return null
    }

    private fun charge(invoice: Invoice): Invoice? {
        try {
            if (paymentProvider.charge(invoice)) {
                return updateStatusToPaid(invoice)
            } else {
                return updateStatusToError(invoice)
            }
        } catch (e: CurrencyMismatchException) {
//            TODO: currency conversion
            logger.error(e.localizedMessage)
            updateStatusToError(invoice)
        } catch (e: CustomerNotFoundException) {
            logger.error(e.localizedMessage)
            updateStatusToError(invoice)
        } catch (e: NetworkException) {
            logger.error(e.localizedMessage)
            updateStatusToError(invoice)
        }
        return null
    }

    private fun updateStatusToPaid(invoice: Invoice): Invoice? {
        return dal.updateInvoiceStatus(invoice.id, InvoiceStatus.PAID)
    }

    private fun updateStatusToError(invoice: Invoice): Invoice? {
        return dal.updateInvoiceStatus(invoice.id, InvoiceStatus.ERROR)
    }
}

