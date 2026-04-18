## 1. Inicializácia hry

Keď sa hra spustí:

- Vytvorí sa herná doska s rozmermi N x N.
- Každej bunke sa náhodne priradí jedna z platných farieb.
- Nastaví sa maximálny počet ťahov (maxMoves).
- Počítadlo ťahov (currentMoves) sa vynuluje.
- Stav hry sa nastaví na PLAYING.

---

## 2. Základná hrateľnosť

Hráč začína hru z ľavého horného políčka ihriska (súradnice **(0,0)**).

### Jedno kolo hry sa vykoná nasledovne:

1. Hráč si vyberie novú farbu.
2. Ak sa vybraná farba zhoduje s aktuálnou farbou východiskovej oblasti, ťah sa nevykoná.
3. V opačnom prípade:
- spustí sa algoritmus **Flood Fill**;
- zmení sa farba celej pripojenej oblasti začínajúcej na **(0,0)**;
- počítadlo ťahov sa zvýši o 1.

---

## 3. Algoritmus zaplavenia

Algoritmus vykonáva nasledujúce kroky:

1. Určuje počiatočnú farbu počiatočnej bunky.

2. Rekurzívne (alebo cez zásobník/front):
- kontroluje susedné bunky (hore, dole, vľavo, vpravo);
- ak má susedná bunka počiatočnú farbu,:
- je prefarbená novou farbou;
- je pridaná do spracovania.

3. Proces pokračuje, kým nie sú spracované všetky prepojené bunky počiatočnej farby.

Týmto sa rozšíri „zachytená“ oblasť.

---

## 4. Kontrola podmienok ukončenia

Po každom ťahu sa kontroluje stav hry.

### Víťazstvo

Ak majú všetky políčka na hracej doske rovnakú farbu,
stav hry sa zmení na `SOLVED`.

### Porážka

Ak počet ťahov dosiahol `maxMoves`,
a hracia doska nie je úplne vyfarbená,
stav hry sa zmení na `FAILED`.

---

## 5. Koniec hry

Hra končí v dvoch prípadoch:

- Hráč vyhráva (celá hracia doska je jednej farby).

- Hráč prehráva (už nie sú možné žiadne ďalšie ťahy).

Po skončení hry sú možné nasledujúce možnosti:

- Reštartovať hru;
- Ukončiť program.

---

# Základné pravidlá hry

- Východiskový bod je vždy pevný — **(0,0)**.
- Povolené sú iba susediace bunky po stranách (žiadne uhlopriečky).
- Každý ťah zmení farbu celej aktuálne prepojenej oblasti.
- Cieľom hry je urobiť hraciu dosku monochromatickou v minimálnom počte ťahov.

# Video GamePlay FloodFill - https://www.youtube.com/watch?v=Zapy1RRSxTs