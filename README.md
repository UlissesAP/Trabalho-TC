# Fluxo Apresentação TC

## Autômatos-iniciais

| Autômato | Linguagem (descrição) |
|---|---|
| **A** | contém "ba"   |
| **B** | termina em "a" | 
| **P** | começa com "a" | 
| **P2** | número par de "1"s | 

## Fluxo

| # | Autômato         | Operação |
|---|------------------|---|
| 1 | **C**            | União(A, B) |
| 2 | **D**            | Diferença(C, B) |
| — | **D_minimizado** | Minimização(D) |
| 3 | **E**            | Interseção(D, P) |
| — | **E_minimizado** | Minimização(E) |
| 4 | **F**            | Reverso(E) |
| 5 | **G**            | Concatenação(F, B) |
| 6 | **H**            | Homomorfismo(G, h) |
| 7 | **H_estrela**    | Estrela(H) |
| 8 | **H_AFD**        | AFN→AFD(H\*) |
| 9 | **I**            | Complemento(H') |
| 10 | **J**            | Diferença Simétrica(I, P2) |
| 11 | **J_minimizado** | Minimização(J) |