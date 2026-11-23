# Ajustes da tela Início (DigiPax)

Arquivo alterado (só este): `composeApp/src/commonMain/kotlin/com/example/pxrioverde/ui/components/HomeComponents.kt` (UTF-8, CRLF mantido: 0 linhas só com LF).
Compilação: `:composeApp:compileDebugKotlinAndroid` OK e `:composeApp:compileKotlinIosArm64` OK. Nada commitado. Nenhum drawable XML novo; só ImageVector já existente (`CorporateIcons.Clock`, `Exit`, `ChevronRight`).

Contraste = fórmula WCAG (luminância relativa). Cores com transparência foram misturadas com o fundo real antes de calcular.

## 1. Barra inferior (grave)
Fundo da barra: `#F5F7F6`.

| Item | Antes | Depois | Contraste antes → depois |
|---|---|---|---|
| Rótulo inativo | `LightGray` #D3D3D3 | #4A554F | 1,39 → **7,22** (meta 4,5) |
| Ícone inativo | `LightGray` #D3D3D3 | #55605A | 1,39 → **6,09** (meta 3) |
| Aba ativa (não mudou) | rótulo #2D6A4F, ícone #1B4332 | igual | 5,94 e 10,30 |

A aba ativa continua mais forte: negrito, verde da marca e a pílula verde translúcida atrás do ícone. Inativas ficam em cinza-esverdeado médio, normal.
Observação: o `tonalElevation` pode tingir levemente o fundo; isso muda o contraste na casa de centésimos, com folga grande sobre a meta.

## 2. "Meu banco de horas" e "Vai se ausentar? Clique aqui"
Antes: `TextButton` com padding 0, texto branco 90% e ícone branco 80%, sem forma de botão.
Agora: novo componente privado `HomeActionRow`, usado pelos dois (mesmo padrão): linha de largura total, altura mínima 48 dp, cantos 12 dp, fundo branco 12% sobre o verde #2D6A4F (≈ #467C64), contorno branco 35% de 1 dp, ícone à esquerda, texto branco pleno e chevron (`CorporateIcons.ChevronRight`) à direita. Textos e navegação iguais. Espaço de 8 dp entre as duas linhas.
Contraste do texto branco sobre a linha tingida: 4,85 (antes 5,53 com branco 90% sobre o card). Continua acima de 4,5 e o texto agora está em branco pleno.

## 3. Atendimento Rápido: retângulo cinza em "Equipamentos"
Investigação: em `QuickCategoryButton` (celular) e `QuickCategoryButtonDesktop` não há fundo condicional nem estado por item; os quatro usam o mesmo código e só o `clickable` padrão. Nenhum fundo fixo existe, então o retângulo é estado de interação (realce de foco/pressionado do `clickable`, que no primeiro item aparece como retângulo do tamanho do bloco inteiro, com cantos retos).
Correção: no `QuickCategoryButton`, o bloco (ícone + rótulo) agora é recortado com `clip(RoundedCornerShape(16.dp))` antes do `clickable`, então qualquer realce de toque/foco sai arredondado, no formato do bloco, e igual nos quatro.
Não foi visto rodando: não consegui reproduzir o retângulo cinza sem um aparelho. Se ele ainda aparecer parado (sem tocar), me avise com o print, pois seria foco de teclado/leitor de tela.

## 4. Textos claros sobre verde e título da seção

| Texto | Antes | Depois | Contraste antes → depois |
|---|---|---|---|
| "Portal Integrado Pax Rio Verde." (sobre #1B4332) | bodySmall, branco 80% | bodyMedium, peso Medium, branco 95% | 7,71 → **10,21** |
| Descrição do Mural (pior ponto do degradê, #2D6A4F) | bodySmall, branco 85% | bodyMedium, peso Medium, branco 95% | 5,16 → **5,97** (no lado #1B4332 é maior) |
| Título "COMUNICADOS & IDEIAS" (sobre #F5F7F6) | #1B4332 a 60%, espaçamento 2 sp | #1B4332 pleno, espaçamento 1 sp | 3,41 → **10,30** |

Textos e a cor da marca (#1B4332) não mudaram; só a opacidade foi removida. O espaçamento do título caiu de 2 para 1 sp para não quebrar linha com o texto maior nas telas estreitas.

## O que não foi visto rodando
- Nenhuma tela foi aberta em aparelho, emulador ou simulador; só compilação (Android e iOS arm64).
- Aparência real da linha tingida, do chevron e do realce arredondado do achado 3.
- Quebra de linha do subtítulo e da descrição do Mural com a fonte maior em telas pequenas.
- Tema de alto contraste/escuro: as cores novas da barra inferior são fixas, como as antigas; não testei esses temas.
- A barra do admin (`AdminDashboard.kt`) tem a mesma estrutura, mas fica fora deste pedido e não foi alterada.
