package com.example.pxrioverde.model

import kotlinx.serialization.Serializable

@Serializable
data class Supervisor(
    val id: String,
    val name: String,
    val email: String
)

object SupervisorData {
    val list = listOf(
        Supervisor("d039e4a5-4dd1-47a7-86f7-7ac0fa90e755", "Eloene", "eloene@pax.com"),
        Supervisor("2d362609-fc50-445a-874d-d53d3b883273", "Leidimar", "leidimar@pax.com"),
        Supervisor("83418b4c-90b2-4c9a-9520-b2b2fb329526", "Luiza", "luiza@pax.com"),
        Supervisor("5015b651-7d70-4327-a184-f735c7062b4d", "Maisa", "maisa@pax.com"),
        Supervisor("ba9dcbc7-2b9b-4c9d-9ffb-00d2fdf21028", "Marcia", "marcia@pax.com"),
        Supervisor("af5749ac-522d-44fa-8ec5-8f31e6cdc416", "Maximilliano", "max@pax.com"),
        Supervisor("d1e74a8a-1896-41e1-9e9a-36d05bef4d48", "Natália", "natalia@pax.com"),
        Supervisor("f2adefc6-e0ed-4a86-b56d-14a99f8709a9", "Renata", "renata.peixoto@pax.com"),
        Supervisor("8e8a74d8-8c48-4274-ac62-be4445f0fc5c", "Valéria", "valeria@pax.com")
    )
}
