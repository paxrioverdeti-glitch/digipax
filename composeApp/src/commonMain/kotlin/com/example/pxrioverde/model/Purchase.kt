package com.example.pxrioverde.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class PurchaseType(val label: String) {
    @SerialName("MATERIAL") MATERIAL("Material"),
    @SerialName("PRODUTO") PRODUTO("Produto"),
    @SerialName("SERVICOS") SERVICOS("Serviços"),
    @SerialName("EQUIPAMENTO") EQUIPAMENTO("Equipamento")
}

@Serializable
enum class UrgencyLevel(val label: String) {
    @SerialName("ALTA") ALTA("Alta"),
    @SerialName("MEDIA") MEDIA("Média"),
    @SerialName("BAIXA") BAIXA("Baixa")
}

@Serializable
enum class PaymentMethod(val label: String) {
    @SerialName("PIX") PIX("Pix"),
    @SerialName("BOLETO") BOLETO("Boleto"),
    @SerialName("DINHEIRO") DINHEIRO("Dinheiro"),
    @SerialName("CARTAO") CARTAO("Cartão")
}

@Serializable
enum class PurchaseStatus(val label: String) {
    @SerialName("PENDENTE") PENDENTE("Pendente"),
    @SerialName("APROVADO") APROVADO("Aprovado"),
    @SerialName("REJEITADO") REJEITADO("Rejeitado"),
    @SerialName("PROGRAMADO") PROGRAMADO("Programado"),
    @SerialName("COMPRADO") COMPRADO("Comprado")
}

@Serializable
data class PurchaseRequest(
    val id: String? = null,
    @SerialName("requesterId") val requesterId: String = "",
    @SerialName("requesterName") val requesterName: String = "",
    @SerialName("department") val department: String = "",
    @SerialName("deliveryLocation") val deliveryLocation: String = "",
    @SerialName("type") val type: PurchaseType = PurchaseType.MATERIAL,
    @SerialName("urgency") val urgency: UrgencyLevel = UrgencyLevel.BAIXA,
    @SerialName("itemName") val itemName: String = "",
    @SerialName("quantity") val quantity: String = "",
    @SerialName("justification") val justification: String = "",
    @SerialName("hasSuggestedVendor") val hasSuggestedVendor: Boolean = false,
    @SerialName("vendorName") val vendorName: String? = null,
    @SerialName("vendorContact") val vendorContact: String? = null,
    @SerialName("technicalDescription") val technicalDescription: String = "",
    @SerialName("paymentmethod") val paymentMethod: PaymentMethod = PaymentMethod.PIX,
    @Transient val hasInvoice: Boolean = false,
    @SerialName("estimatedValue") val estimatedValue: Double? = null,
    @SerialName("approvedValue") val approvedValue: Double? = null,
    @SerialName("attachmentUrl") val attachmentUrl: String? = null,
    @SerialName("status") val status: PurchaseStatus = PurchaseStatus.PENDENTE,
    @SerialName("managerComment") val managerComment: String? = null,
    @SerialName("createdAt") val createdAt: Long = 0L,
    @SerialName("approvalDate") val approvalDate: Long? = null,
    @SerialName("approverName") val approverName: String? = null
)

@Serializable
data class PurchaseRequestUpdate(
    @SerialName("status") val status: String,
    @SerialName("managerComment") val managerComment: String? = null,
    @SerialName("approvedValue") val approvedValue: Double? = null,
    @SerialName("approvalDate") val approvalDate: Long,
    @SerialName("approverName") val approverName: String? = null
)
