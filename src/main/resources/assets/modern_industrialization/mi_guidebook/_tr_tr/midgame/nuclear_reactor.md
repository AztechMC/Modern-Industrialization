---
navigation:
  title: "Nükleer Reaktör"
  icon: "modern_industrialization:uranium_fuel_rod_quad"
  position: 206
  parent: modern_industrialization:midgame.md
item_ids:
  - modern_industrialization:nuclear_reactor
  - modern_industrialization:nuclear_casing
  - modern_industrialization:nuclear_item_hatch
  - modern_industrialization:nuclear_fluid_hatch
---

# Nükleer Reaktör

Nükleer Reaktör, amacı nükleer yakıt tüketerek büyük miktarlarda enerji üretmek olan büyük bir çoklu bloktur. Dizele göre 100'lerce kat daha fazla EU/t üretebilir. Küçükten büyüğe çeşitli boyutlarda mevcuttur.

<Recipe id="modern_industrialization:electric_age/machine/nuclear_reactor_asbl" />

Nükleer Reaktör kullanarak güç üretmek için Nükleer Sıvı Kapağına bir çeşit Su yerleştirmeniz gerekir. Buhar haline dönüşecek ve muhtemelen önce bir Isı Değiştiriciden geçirilerek güç üretmek için kullanılabilecektir.

Nükleer Reaktör ayrıca Plütonyum gibi bazı malzemeleri üretmenin tek yoludur.

Son olarak, nükleer füzyon için kullanılan sıvıları üretebilir: Döteryum ve Trityum.

İlerleyen sayfalarda açıklanmaya çalışılmasına rağmen, nasıl çalıştığını anlamak biraz bunaltıcı olabilir. Bununla birlikte, güçlü bir reaktör tasarlayabilmek için ayrıntıları anlamanıza gerek yoktur.

Size uygun olanı bulana kadar yaratıcı modda çeşitli tasarımları denemenizi öneririz.

Nükleer Reaktörün patlayamayacağını, radyasyon yayamayacağını veya haritaya başka bir şekilde zarar veremeyeceğini unutmayın. Zarar verebileceği tek şey, sıcaklık çok yükselirse içine koyduğunuz eşyalardır (bu konuda daha sonra bilgi verilecektir).

Deney yapmaktan çekinmemeli ve bir nükleer bilimci olarak yeni hayatınızın tadını çıkarmalısınız!

Ana bileşen, kadmiyum, berilyum ve patlamaya dayanıklı alaşımın bir karışımı olan Nükleer Alaşım ile yapılan Nükleer Muhafazadır.

<Recipe id="modern_industrialization:electric_age/casing/nuclear_casing_asbl" />

Yapının üst kısmı Nükleer Eşya veya Sıvı Kapaklarını (veya basit muhafazaları) kabul eder. Bunlar reaktörün giriş ve çıkışlarıdır.

<Recipe id="modern_industrialization:electric_age/casing/nuclear_item_hatch_asbl" />

Her kapağın bir girişi (eşya ya da sıvı) ve iki çıkışı vardır. Giriş yuvaları, reaktör GUI'sinde görüntülenen bir ızgara oluşturacaktır (denetleyiciye sağ tıklayarak erişilebilir).

<Recipe id="modern_industrialization:electric_age/casing/nuclear_fluid_hatch_asbl" />

Her kapak bir sıcaklığa sahiptir ve çeşitli şekillerde dağıtılabilen ısıyı depolar. Isı doğal olarak bitişik bir kapağa veya kapak kenarda ise dışarıya doğru hareket edecektir (ısı daha sonra kaybedilir). Bu sürecin hızı, kapağın içeriğinin ısı aktarım katsayısının (REI'de gösterilmiştir) sıcaklık farkıyla çarpımına eşittir. Isı, buhar üretilerek bir sıvı kapağında da elde edilebilir. Bir eşyanın maksimum sıcaklığının üzerindeki bir sıcaklık onu yok edecektir.

Nükleer reaktörün temel elementleri nötronlardır. Bunlar nükleer yakıt tarafından üretilir. Hızlı ve ısıl olmak üzere iki tür nötron vardır: hızlı nötronlar enerji taşırken ısıl nötronlar taşımaz. Nötronlar bir elementle karşılaşana ya da reaktörden çıkana kadar düz bir çizgi üzerinde hareket ederler (hızlı nötronlar için enerjileri daha sonra kaybolur). Nötronların akışı reaktör GUI'sinde gösterilir.

Bir nötron boş olmayan bir kapakla karşılaştığında iki şey olabilir: nötron emilir veya saçılır. Saçılan bir nötron rastgele yön değiştirecektir. Saçılan nötron hızlıysa, yavaşlayarak ısıl bir nötron haline gelme şansı vardır. Bu, enerjiyi nötrondan ısı şeklinde kapak içine aktarır. Soğurulan bir nötron rotasını durdurur ve eğer hızlı bir nötron ise enerjisini de aktarır.

Tek bir kapakta emilen nötronların sayısı GUI'de görülebilir. Her işlem için olasılık REI'de gösterilir. Bunlar büyük ölçüde kapağın içeriğine ve nötron türüne (hızlı veya ısıl) bağlıdır. Nükleer yakıtlar ısıl nötronları çok daha iyi emer.

Nükleer yakıtta hızlı ya da ısıl bir nötron emildiğinde, daha fazla nötron üretilir. Bunlar her zaman hızlı nötronlardır ve rastgele yönleri vardır. Bunların oluşumuna, kapakta ek ısı şeklinde doğrudan enerji salınımı eşlik eder.

Belirli bir eşiğin üzerinde, üretilen nötronların sayısı sıfıra ulaşana kadar sıcaklıkla birlikte azalacaktır. Bu süreç bir miktar enerji israfına neden olur, ancak reaktörün kararlılığını garanti eder. Üretilen nötronların sayısı (ve etkin verimlilik), doğrudan enerji ve sıcaklık eşiği REI'de.

Her nükleer bileşenin maksimum emilim sayısı vardır. Bu sayıya ulaşıldığında, eşya ya yok edilir ya da tükenmiş bir versiyona dönüştürülür. Bu özellikle nükleer yakıt için kullanışlıdır çünkü U238'in bir kısmı tükenmiş versiyonda Plütonyuma dönüşür, yani bir kısmı tekrar yakıta dönüştürülebilir.

Aynı şey sıvılar için de geçerlidir: her nötron emiliminden sonra bir miktar sıvı dönüşüme uğrar. Bu, döteryum ve trityum gibi faydalı izotopları seri olarak üretmek için kullanılabilir. Her iki durumda da sonuç REI'de gösterilmiştir.

