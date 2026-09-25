package com.example.pxrioverde.repository

import com.example.pxrioverde.model.PurchaseRequest
import com.example.pxrioverde.model.PurchaseRequestUpdate
import com.example.pxrioverde.model.PurchaseStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.datetime.Clock

class PurchaseRepository(
    private val supabase: SupabaseClient
) {
    suspend fun submitPurchaseRequest(request: PurchaseRequest): PurchaseRequest {
        val now = Clock.System.now().toEpochMilliseconds()
        val payload = request.copy(createdAt = now)
        
        return supabase.from("purchase_requests").insert(payload) {
            select()
        }.decodeSingle<PurchaseRequest>()
    }

    suspend fun getRequestsByRequester(userId: String): List<PurchaseRequest> {
        return try {
            supabase.from("purchase_requests")
                .select {
                    filter {
                        eq("requesterId", userId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<PurchaseRequest>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAllRequests(): List<PurchaseRequest> {
        return try {
            supabase.from("purchase_requests")
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<PurchaseRequest>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getRequestsByPeriod(month: Int, year: Int): List<PurchaseRequest> {
        // Filtro simplificado: Buscamos todos e filtramos no cliente para KMP compatibility
        // ou usamos postgrest filters se as colunas permitirem.
        // Como o createdAt é Long (timestamp), vamos filtrar via código no UseCase/ViewModel para Senior performance.
        return getAllRequests()
    }

    suspend fun updateRequestStatus(
        requestId: String, 
        status: PurchaseStatus, 
        comment: String?,
        approvedValue: Double? = null,
        approverName: String? = null
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        val payload = PurchaseRequestUpdate(
            status = status.name,
            managerComment = comment,
            approvedValue = approvedValue,
            approvalDate = now,
            approverName = approverName
        )
        supabase.from("purchase_requests").update(payload) {
            filter {
                eq("id", requestId)
            }
        }
    }

    suspend fun uploadAttachment(bytes: ByteArray, fileName: String): String {
        val path = "purchases/${Clock.System.now().toEpochMilliseconds()}_$fileName"
        val bucket = supabase.storage.from("attachments")
        bucket.upload(path, bytes)
        return bucket.publicUrl(path)
    }
}
