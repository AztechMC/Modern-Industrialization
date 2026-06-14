---
navigation:
  title: "Tubos"
  icon: "modern_industrialization:wrench"
  position: 4
  parent: modern_industrialization:steam_age.md
---

# Tubos

Tubulações são usadas nas receitas de algumas máquinas, mas também podem ser colocadas no mundo. Até 3 tubos podem caber em um único bloco.

Tubos de cores diferentes não se conectam.

Clicar com o botão direito em um tubo segurando uma chave inglesa muda o tipo de conexão; clicando com shift e botão direito fará o tubo cair.

## Tubos de Fluido

<ItemImage id="modern_industrialization:fluid_pipe" />

Tubos de fluido só podem conter um tipo de fluido, e por padrão não se conectam a tubos vazios ou blocos. Você pode forçar a conexão clicando no centro do tubo com a chave inglesa, ou clicando em um bloco segurando um tubo na mão: isso conecta sem consumir o item.

## Tubos de Itens

<ItemImage id="modern_industrialization:item_pipe" />

Tubos de itens não se conectam a inventários por padrão, mas você pode forçar a conexão.

Clicando em uma conexão sem segurar a chave inglesa, você verá um filtro. Por padrão, o modo whitelist (lista branca) está ativado, então nenhum item será inserido ou extraído diretamente.

Os diferentes tipos de tubos funcionam de maneira diferente.

A cada tick, tubos de fluido tentam extrair igualmente de todos os blocos conectados e depois inserir de forma equilibrada. [Leia mais sobre a velocidade deles aqui.](../midgame/fluid_transfer.md)

Tubos de itens inserem itens primeiro em prioridades mais altas e transferem 16 itens a cada poucos segundos. **Eles podem ser atualizados com motores.**

## Nota

Tubos de itens estão no modo Lista Branca por padrão (ou seja, apenas os itens no filtro serão movidos). Você precisa mudar para Lista Negra (apenas itens NÃO no filtro serão movidos) ou adicionar o item que quer mover ao filtro. Cuidado: ambos os lados do tubo precisam ser configurados, ou os itens não se moverão.

