# Walkthrough - Fluxo de Aprovação Descentralizado

Implementei um sistema de aprovação inteligente e hierárquico para os comunicados de ausência. Agora, os colaboradores podem direcionar suas solicitações para supervisores específicos, e estes ganharam um portal exclusivo para gerenciar essas aprovações.

## Mudanças Realizadas

### 1. Modelo de Dados e Segurança
- **Novos Papéis (`UserRole`)**: Adicionados os perfis `SUPERVISOR` e `ENCARREGADO`.
- **Direcionamento**: O comunicado agora registra quem é o aprovador alvo (`targetApproverId` e `targetApproverName`).

### 2. Experiência do Colaborador (Formulário)
- **Seletor de Aprovador**: No formulário de ausência, foi adicionado o campo **"Encaminhar para aprovação de:"**.
- **Lista Dinâmica**: O app busca automaticamente todos os usuários cadastrados como Supervisor, Encarregado ou Admin no sistema para exibir na lista.

### 3. Portal de Aprovação (Supervisores)
- **Aba "Aprovar"**: Supervisores e Encarregados agora têm uma aba dedicada na barra inferior (Scaffold).
- **Filtro Inteligente**: Neste portal, o supervisor vê **apenas** as solicitações que foram direcionadas a ele, mantendo o foco no seu time.
- **Ação Direta**: O supervisor pode avaliar, autorizar e dar o parecer final diretamente pelo app.

### 4. Gestão Administrativa (RH/Admin)
- O RH continua tendo a visão global de todos os comunicados através do Painel Admin existente, garantindo a transparência total do processo.

## Como Testar

1. **Definir Papéis**: No seu banco de dados Supabase (tabela `profiles`), mude o `role` de alguns usuários para `SUPERVISOR` ou `ENCARREGADO`.
2. **Enviar Solicitação**: Como um usuário comum (`CLIENT`), abra um comunicado e selecione um dos supervisores na lista.
3. **Validar Recebimento**: Entre com a conta do supervisor escolhido e verifique se a nova aba **"Aprovar"** aparece e contém a solicitação.
4. **Validar Isolamento**: Entre com a conta de outro supervisor e confirme que ele **não vê** o pedido direcionado ao primeiro.

> [!IMPORTANT]
> Certifique-se de que os nomes dos papéis no banco de dados estejam em letras maiúsculas (`SUPERVISOR`, `ENCARREGADO`) para que o app os reconheça corretamente.

> [!TIP]
> O RH (Admin) também aparece na lista de aprovadores e tem acesso ao Portal de Aprovação, servindo como backup para casos onde o supervisor imediato não estiver disponível.
