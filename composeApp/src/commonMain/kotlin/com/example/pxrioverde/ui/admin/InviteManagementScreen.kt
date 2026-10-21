package com.example.pxrioverde.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pxrioverde.service.AllowedEmailDto
import com.example.pxrioverde.service.AuthService
import com.example.pxrioverde.ui.components.CorporateIcons
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Tela do painel admin para convidar funcionarios (Primeiro acesso - Caminho 2).
 * So o e-mail convidado aqui consegue criar login no app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteManagementSection(isDesktop: Boolean = false, refreshRequest: Int = 0) {
    val authService: AuthService = koinInject()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("CLIENT") }
    var isSaving by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var invites by remember { mutableStateOf<List<AllowedEmailDto>>(emptyList()) }

    suspend fun refresh() { invites = authService.getInvites() }

    LaunchedEffect(refreshRequest) { refresh() }

    val roles = listOf(
        "CLIENT" to "Funcionário",
        "ENCARREGADO" to "Encarregado",
        "SUPERVISOR" to "Supervisor",
        "ADMIN" to "Administrador"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text(
            "Convidar funcionário",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1B4332)
        )
        Text(
            "Só quem for convidado aqui consegue criar login no Primeiro acesso.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; feedback = null },
            label = { Text("E-mail do funcionário") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome (opcional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text("Papel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            roles.forEach { (value, label) ->
                FilterChip(
                    selected = role == value,
                    onClick = { role = value },
                    label = { Text(label) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val trimmed = email.trim()
                if (!trimmed.contains("@") || !trimmed.contains(".")) {
                    feedback = "Digite um e-mail válido."
                    isError = true
                    return@Button
                }
                isSaving = true
                feedback = null
                scope.launch {
                    try {
                        authService.inviteEmail(trimmed, name.ifBlank { null }, role)
                        feedback = "Convite criado! $trimmed já pode fazer o Primeiro acesso."
                        isError = false
                        email = ""
                        name = ""
                        refresh()
                    } catch (e: Exception) {
                        feedback = e.message ?: "Não foi possível convidar."
                        isError = true
                    } finally {
                        isSaving = false
                    }
                }
            },
            enabled = !isSaving && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332), contentColor = Color.White)
        ) {
            if (isSaving) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("CONVIDAR", fontWeight = FontWeight.Bold)
            }
        }

        if (feedback != null) {
            Spacer(Modifier.height(12.dp))
            Surface(
                color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        else Color(0xFF1B4332).copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    feedback!!,
                    color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF1B4332),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(Modifier.height(16.dp))

        Text(
            "Convidados (${invites.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))

        if (invites.isEmpty()) {
            Text(
                "Nenhum convite ainda.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(invites) { invite ->
                    InviteRow(
                        invite = invite,
                        onRemove = {
                            scope.launch {
                                try {
                                    authService.removeInvite(invite.email)
                                    refresh()
                                } catch (e: Exception) {
                                    feedback = "Erro ao remover: ${e.message}"
                                    isError = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InviteRow(invite: AllowedEmailDto, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    invite.name ?: invite.email,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    invite.email,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (invite.used) Color(0xFF1B4332).copy(alpha = 0.15f)
                        else Color(0xFFF0A202).copy(alpha = 0.18f)
            ) {
                Text(
                    if (invite.used) "Ativo" else "Pendente",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (invite.used) Color(0xFF1B4332) else Color(0xFF8A6D00),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remover convite",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
