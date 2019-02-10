# QRLudo

QR Ludo est un lecteur de QR codes qui permet d'entendre le contenu des QR codes scannés. Cette application mobile est dédiée au personnes atteintes de déficiences visuelles. 
Elle sert principalement de support de cours pour des élèves aveugles et mal-voyants.

Les QR codes à utiliser avec QR Ludo doivent être générés avec l'application de bureau QR Ludo Generateur (disponible [ici](https://github.com/univ-angers/QRLudo-Generator/))

## Installation

QR Ludo est disponible en téléchargement sur le Google Play Store.

## Utilisation

### Manuel utilisateur

Un manuel utilisateur est disponible à l'adresse : https://github.com/univ-angers/QRLudo-Generator/blob/master/docs/qrludo_manuel_utilisateur.pdf

### Types de QRCodes

Il existe plusieurs types de QR codes pour des usages différents. L'application QR Ludo se comportera différemment selon le QR code détecté.

#### QRCode unique

Le scan de ce type de QRCode peut posséder du texte ou des fichiers audio.
Le texte est lu à l'aide d'une synthèse vocale, les fichiers audio sont téléchargés (s'ils ne sont pas présents sur le téléphone) puis joués.

#### QRCode ensemble

Ces QRCodes ne contiennent que des QRCodes contenant des fichiers. Le scan de ces QRCodes entraîne le téléchargement de tous les fichiers.

#### QRCode Question / Réponse

Pour faire fonctionner le système des questions/réponses, il faut d'abord scanner un qrcode Question. La question est alors lue par synthèse vocale. L'application attend ensuite qu'un qrcode Réponse soit scanné.
Un message est alors lu pour indiquer si la réponse scanée est la bonne ou non.

Les qrcodes Réponse peuvent être scanné pour entendre le texte de la réponse.

### Contexte du développement
Ce projet a eu lieu dans le cadre des modules d'enseignement Management de Projet et Concrétisation Disciplinaire suivis par les étudiants de Master 2 Informatique ACDI (Analyse, Conception et Développement Informatique) et de Master 1 Informatique de l'Université d'Angers de l'année universitaire 2018/2019.

## Règles de confidentialité

Disponibles [ici](https://github.com/univ-angers/QRLudo/blob/master/regles_confidentialite.md).
