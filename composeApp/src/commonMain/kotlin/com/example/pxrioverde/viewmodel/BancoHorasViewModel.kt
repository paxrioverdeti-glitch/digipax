package com.example.pxrioverde.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pxrioverde.model.BancoHoras
import com.example.pxrioverde.model.BancoHorasErro
import com.example.pxrioverde.repository.BancoHorasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BancoHorasState {
    object Carregando : BancoHorasState()
    data class Pronto(val dados: BancoHoras) : BancoHorasState()
    data class Erro(val mensagem: String) : BancoHorasState()
}

/** Guarda o saldo só na memória, e só enquanto a tela está aberta. */
class BancoHorasViewModel(
    private val repository: BancoHorasRepository
) : ViewModel() {
    private val _estado = MutableStateFlow<BancoHorasState>(BancoHorasState.Carregando)
    val estado: StateFlow<BancoHorasState> = _estado.asStateFlow()

    fun carregar() {
        _estado.value = BancoHorasState.Carregando
        viewModelScope.launch {
            repository.consultar().fold(
                onSuccess = { _estado.value = BancoHorasState.Pronto(it) },
                onFailure = {
                    _estado.value = BancoHorasState.Erro(
                        (it as? BancoHorasErro)?.mensagem
                            ?: "Não conseguimos buscar seu saldo agora. Tente de novo."
                    )
                }
            )
        }
    }

    /** Chamado ao sair da tela e ao fazer logout: não deixa saldo na memória. */
    fun limpar() {
        _estado.value = BancoHorasState.Carregando
    }
}
