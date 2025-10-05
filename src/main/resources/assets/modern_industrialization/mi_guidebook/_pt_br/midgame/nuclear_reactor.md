---
navigation:
  title: "O Reator Nuclear"
  icon: "modern_industrialization:uranium_fuel_rod_quad"
  position: 206
  parent: modern_industrialization:midgame.md
item_ids:
  - modern_industrialization:nuclear_reactor
  - modern_industrialization:nuclear_casing
  - modern_industrialization:nuclear_item_hatch
  - modern_industrialization:nuclear_fluid_hatch
---

# O Reator Nuclear

<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="nuclear_reactor" />
    <MultiblockShape controller="nuclear_reactor" useBigShape={true} x="-11" z="-3" />
</GameScene>

O Reator Nuclear é um grande multibloco cujo propósito é gerar enormes quantidades de energia consumindo combustível nuclear. Ele pode produzir centenas de vezes mais EU/t do que o diesel. Existem versões de tamanhos variados, de pequenas a grandes.

<Recipe id="modern_industrialization:electric_age/machine/nuclear_reactor_asbl" />

Para gerar energia com o Reator Nuclear, você precisa colocar algum tipo de água em uma Escotilha de Fluidos Nucleares. Ela será convertida em vapor, que pode ser usado para gerar energia, possivelmente passando antes por um Trocador de Calor.

O Reator Nuclear também é a única maneira de produzir alguns materiais, como o Plutônio.

Por fim, ele pode gerar fluidos usados na fusão nuclear: Deutério e Trítio.

O funcionamento interno pode ser um pouco complexo, apesar das páginas seguintes tentarem explicar. No entanto, você não precisa entender todos os detalhes para conseguir projetar um reator eficiente.

Recomendamos que você experimente diferentes designs no modo criativo até encontrar um que atenda às suas necessidades.

Observe que o Reator Nuclear não explode, não emite radiação e não danifica o mapa. A única coisa que ele pode danificar são os itens colocados nele, caso a temperatura fique muito alta (mais sobre isso adiante).

Sinta-se livre para experimentar e aproveite sua nova vida como cientista nuclear!

O componente principal é a Estrutura Nuclear, feita com Liga Nuclear, uma mistura de cádmio, berílio e liga à prova de explosões.

<Recipe id="modern_industrialization:electric_age/casing/nuclear_casing_asbl" />

A parte superior da estrutura aceita Escotilhas Nucleares de Itens ou Fluidos (ou estruturas simples). Elas funcionam como entradas e saídas do reator.

<Recipe id="modern_industrialization:electric_age/casing/nuclear_item_hatch_asbl" />

Cada escotilha possui uma entrada (item ou fluido) e duas saídas. Os slots de entrada formam uma grade exibida na interface do reator (acessível com clique direito no controlador).

<Recipe id="modern_industrialization:electric_age/casing/nuclear_fluid_hatch_asbl" />

Os elementos principais do reator são os nêutrons, produzidos pelo combustível nuclear. Existem dois tipos: rápidos e térmicos. Nêutrons rápidos carregam energia; térmicos não. Eles se movem em linha reta até encontrarem um elemento ou saírem do reator (nêutrons rápidos perdem sua energia nesse caso). O fluxo de nêutrons é mostrado na interface do reator.

Quando um nêutron atinge uma escotilha não vazia, duas coisas podem acontecer: ele é absorvido ou espalhado. Um nêutron espalhado muda de direção aleatoriamente. Se for rápido, pode se tornar térmico, transferindo sua energia para a escotilha na forma de calor. Um nêutron absorvido para sua trajetória e também transfere energia (caso seja rápido).

A quantidade de nêutrons absorvidos por escotilha pode ser vista na interface. As probabilidades desses processos são mostradas no JEI e variam bastante de acordo com o conteúdo da escotilha e o tipo de nêutron. Combustíveis nucleares absorvem muito melhor nêutrons térmicos.

Quando um nêutron (rápido ou térmico) é absorvido por combustível nuclear, mais nêutrons são gerados. Eles são sempre rápidos e saem em direções aleatórias. A geração é acompanhada de liberação direta de energia na forma de calor adicional na escotilha.

Acima de certo limite, a quantidade de nêutrons gerados começa a diminuir conforme a temperatura sobe, até chegar a zero. Esse processo desperdiça energia, mas garante a estabilidade do reator. A quantidade de nêutrons gerados (e eficiência efetiva), energia direta e limite de temperatura estão disponíveis no JEI.

Cada componente nuclear tem um número máximo de absorções. Quando esse número é alcançado, o item é destruído ou transformado em uma versão esgotada. Isso é especialmente útil para o combustível nuclear, pois parte do U238 se transforma em Plutônio na versão esgotada, o que permite reaproveitá-lo como combustível.

O mesmo acontece com fluidos: uma pequena quantidade do fluido é transformada a cada absorção de nêutron. Isso permite a produção em massa de isótopos úteis como Deutério e Trítio. Em ambos os casos, os resultados estão disponíveis no JEI.

