# QRLudo

QR Ludo est un lecteur de QR codes qui permet d'entendre le contenu des QR codes scannés. Cette application mobile est dédiée au personnes atteintes de déficiences visuelles. 
Elle sert principalement de support de cours pour des élèves aveugles et mal-voyants.

Les QR codes à utiliser avec QR Ludo doivent être générés avec l'application de bureau QR Ludo Generateur (disponible [ici](https://github.com/univ-angers/QRLudo-Generator/))

QR Ludo est disponible en téléchargement sur le Google Play Store.

## Types de QRCodes

L'application se comporte différemment selon le QRCode scanné.

### QRCode unique

Le scan de ce type de QRCode peut posséder du texte ou des fichiers audio.
Le texte est lu à l'aide d'une synthèse vocale, les fichiers audio sont téléchargés (s'ils ne sont pas présents sur le téléphone) puis joués.

### QRCode ensemble

Ces QRCodes ne contiennent que des QRCodes contenant des fichiers. Le scan de ces QRCodes entraîne le téléchargement de tous les fichiers.

### QRCode Question / Réponse

Pour faire fonctionner le système des questions/réponses, il faut d'abord scanner un qrcode Question. La question est alors lue par synthèse vocale. L'application attend ensuite qu'un qrcode Réponse soit scanné.
Un message est alors lu pour indiquer si la réponse scanée est la bonne ou non.

Les qrcodes Réponse peuvent être scanné pour entendre le texte de la réponse.


## Contexte du développement
Projet du module Concrétisation Disciplinaire - Master I Informatique - Université d'Angers - 2018/2019

Suite du projet de fin de Master I de [Corentin Talarmain](https://github.com/CorTal/QRLudo) et de [Jules Leguy](https://github.com/juleguy/QRLudo)

Application android permettant d'interpréter les QR Codes générés par [QRLudo-Generator](https://github.com/vrahier/QRLudo-Generator/).
