package com.example.pxrioverde.repository

import com.example.pxrioverde.model.ComunicadoState
import com.example.pxrioverde.model.AdminFeedback
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
private data class AbsenceInsert(
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("user_name") val userName: String,
    @kotlinx.serialization.SerialName("sector") val sector: String,
    @kotlinx.serialization.SerialName("target_approver_id") val targetApproverId: String?,
    @kotlinx.serialization.SerialName("target_approver_name") val targetApproverName: String?,
    @kotlinx.serialization.SerialName("type") val type: String,
    @kotlinx.serialization.SerialName("date") val date: String,
    @kotlinx.serialization.SerialName("expected_time") val expectedTime: String?,
    @kotlinx.serialization.SerialName("effective_time") val effectiveTime: String?,
    @kotlinx.serialization.SerialName("exit_time") val exitTime: String?,
    @kotlinx.serialization.SerialName("return_time") val returnTime: String?,
    @kotlinx.serialization.SerialName("missing_hours") val missingHours: String?,
    @kotlinx.serialization.SerialName("missing_days") val missingDays: String?,
    @kotlinx.serialization.SerialName("original_time") val originalTime: String?,
    @kotlinx.serialization.SerialName("new_time") val newTime: String?,
    @kotlinx.serialization.SerialName("reason") val reason: String?,
    @kotlinx.serialization.SerialName("created_at") val createdAt: Long
)

class AbsenceRepository(
    private val supabase: SupabaseClient
) {
    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var realtimeChannel: RealtimeChannel? = null

    suspend fun submitAbsence(absence: ComunicadoState) {
        val now = Clock.System.now().toEpochMilliseconds()

        val payload = AbsenceInsert(
            userId = absence.userId,
            userName = absence.userName,
            sector = absence.sector,
            targetApproverId = absence.targetApproverId,
            targetApproverName = absence.targetApproverName,
            type = absence.type.name,
            date = absence.date,
            expectedTime = absence.expectedTime,
            effectiveTime = absence.effectiveTime,
            exitTime = absence.exitTime,
            returnTime = absence.returnTime,
            missingHours = absence.missingHours,
            missingDays = absence.missingDays,
            originalTime = absence.originalTime,
            newTime = absence.newTime,
            reason = absence.reason,
            createdAt = now
        )

        supabase.from("absences").insert(payload)
    }

    suspend fun getAbsencesByUser(userId: String): List<ComunicadoState> {
        return try {
            supabase.from("absences_api")
                .select {
                    filter {
                        eq("userId", userId)
                    }
                    order("createdAt", Order.DESCENDING)
                }
                .decodeList<ComunicadoState>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAllAbsences(): List<ComunicadoState> {
        return try {
            supabase.from("absences_api")
                .select {
                    order("createdAt", Order.DESCENDING)
                }
                .decodeList<ComunicadoState>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAbsencesForApprover(approverId: String): List<ComunicadoState> {
        return try {
            supabase.from("absences_api")
                .select {
                    filter {
                        eq("targetApproverId", approverId)
                    }
                    order("createdAt", Order.DESCENDING)
                }
                .decodeList<ComunicadoState>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateFeedback(absenceId: String, feedback: AdminFeedback) {
        // Usamos o objeto diretamente, o SDK do Supabase cuida da serialização para jsonb
        supabase.from("absences").update(
            mapOf("admin_feedback" to feedback)
        ) {
            filter {
                eq("id", absenceId)
            }
        }
    }

    suspend fun subscribeToAbsences(onUpdate: () -> Unit) {
        realtimeChannel?.unsubscribe()
        
        val channel = supabase.realtime.channel("absences_changes")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "absences"
        }

        repositoryScope.launch {
            flow.collect {
                onUpdate()
            }
        }

        channel.subscribe()
        realtimeChannel = channel
    }
}
