# Mecaniques del joc

## Pinguí

### Moviment horitzontal

El pinguí es pot moure cap a l'esquerra i cap a la dreta quan es troba sobre passarel·les o damunt de blocs/enemics.

### Escalar

El pinguí pot pujar i baixar per escales utilitzant el moviment vertical. El joc no inclou salt.

### Caure

El pinguí esta afectat per la gravetat. Cau sempre que no tingui cap suport sota seu.

### Rompre blocs

El pinguí disposa d'una accio especial per foradar un bloc inferior lateral. El forat creat es regenera despres d'uns torns.

### Interaccio amb l'entorn

El pingui pot presionar botons amb el seu pes.
Tambe pot moure el tipus de bloc stone empenyent-lo cap als costats, aquest bloc tambe es afectat per gravetat

### Objectes

Pot usar objectes: llançaflames i teletransport.
                                                                                            
## Enemics

### Enemic simple

L'enemic simple segueix el pinguí quan es troba a la mateixa columna i te visio d'ell, quan deixa de veurel torna a la casella de spawn si pot (amb path)

### Enemics amb pathfinding

Alguns enemics utilitzen pathfinding per calcular una ruta cap al pinguí o altres llocs.

### Enemic seeker

Cerca i persegueix al pinguí, si no pot arribar-hi queda proxim

### Enemic ambush

Cerca i prepara emboscades al pinguí, sempre que pot l'espera dues caselles enfront, en cas d'estar a dues caselles de distancia va a per ell, si no el pot trobar queda proxim

### Enemic Icecream

Cerca i agafa un gelat del mapa, en tenir-lo escapa del jugador, aquest enemic no mata a l'usuari amb contacte directa, quan mor amolla el gelat a la casella de damunt.