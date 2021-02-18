# QRLudo

QR Ludo est un lecteur de QR codes qui permet d'entendre le contenu des QR codes scannés. Cette application mobile est dédiée au personnes atteintes de déficiences visuelles. 
Elle sert principalement de support de cours pour des élèves aveugles et mal-voyants.

Les QR codes à utiliser avec QR Ludo doivent être générés avec l'application de bureau QR Ludo Generateur (disponible [ici](https://github.com/univ-angers/QRLudo-Generator/))

## Installation

QR Ludo est disponible en téléchargement sur le Google Play Store.

## Utilisation

### Site Web

Le site web de QR Ludo (accessible [ici](http://www.univ-angers.fr/qrludo) contient les liens vers les installateurs pour différentes plateformes ainsi que des liens vers vidéos de tutoriels.

### Types de QRCodes

Il existe plusieurs types de QR codes pour des usages différents. L'application QR Ludo se comportera différemment selon le QR code détecté.

#### QRCode unique

Le scan de ce type de QRCode peut posséder du texte ou des fichiers audio.
Le texte est lu à l'aide d'une synthèse vocale, les fichiers audio sont téléchargés (s'ils ne sont pas présents sur le téléphone) puis joués.

#### QRCode Multiple

Ces QRCodes ne contiennent que des QRCodes Uniques. Le scan de ces QRCodes entraîne le téléchargement de tous les fichiers.

#### QRCode Exercice

Exercice ludique qui permet à l'étudiant d'apprendre tout en lui permettant l'erreur.

#### QRCode Reconnaissance Vocale

Exercice à répondre grâce à la reconnaissance vocale.

#### QRCode Serious Game

Un jeu sérieux qui permet d'apprendre en s'amusant.

### Contexte du développement
Le développement de la version actuelle en Kotlin est un redéveloppement (auteur Vincent Barichard) basé sur le travail d'étudiants de l'Université d'Angers. Ces étudiants ont développé la version initiale dans le cadre des modules d'enseignement Management de Projet et Concrétisation Disciplinaire suivis par les étudiants de Master 2 Informatique ACDI (Analyse, Conception et Développement Informatique) et de Master 1 Informatique de l'Université d'Angers.

# Règles de confidentialité

Disponibles [ici](https://github.com/univ-angers/QRLudo/blob/master/regles_confidentialite.md).

# Licence
Copyright (C) 2021 Vincent Barichard, Etienne Choveau, Desnoes Mathis, Kevin Balavoine, ATTAOUI Zakaria, BENDAOUD Youness, 
CUEGNIET Louis, DELÉPINE Pierre-Yves, MITATY Simon
Copyright (C) 2020  Bastien Pigache, Anaïs Mohr, Marouwane Bah, Etienne Choveau, Desnoes Mathis,
Andry Randriamamonjisoa, Kevin Balavoine
Copyright (C) 2019  Thibault Condemine, Alassane Diop, Hanane Hadji, Abdessabour Harboul, Florian Lherbeil,
Jérôme Martins Mosca, Valentine Rahier, Salim Youssef

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

