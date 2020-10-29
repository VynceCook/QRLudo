# Serious Game

Le Serious Game est un jeu qui consiste à faire vivre une aventure à l'utilisateur pendant laquelle il est invité à résoudre plusieurs énigmes.

## Guide d'utilisation

### Introduction

Pour lancer le jeu il faudra tout d'abord scanner un QRCode de type SeriousGame. Une fois celui-ci scanné cela va lancer la lecture de l'introduction du scénario.

### Lancement du scénario

Lorsque la lecture de l'introduction sera fini, il faudra effectuer un balayage de la droite vers la gauche pour que le jeu vous énonce les différentes destinations possibles. Ensuite cela lancera automatiquement la détection de la reconnaissance vocale afin d'énoncé la destination voulue.

### Détection de la destination

Si le mot reconnu n'est pas une destination, cela vous indique que la destination est incorrecte et vous relis les différentes destinations possibles. Pour donner une nouvelle destination il faudra effectuer un balayage de la droite vers la gauche pour lancer la reconnaissance vocale.

Si le mot a bien été reconnu vous serez dirigé vers la destination et une énigme vous sera alors proposé. Une fois l'énigme résolu ou non, vous reviendrez à l'énoncé des différentes destination possible et une nouvelle vous sera alors demandé. Pour choisir la destination il faudra effectuer un balayage de la droite vers la gauche pour lancer la reconnaissance vocale.

Si le mot a bien été reconnu mais que l'énigme de cette destination a déjà été résolu vous serez alors redirigé en arrière et il vous sera demandé de choisir une autre destination. Pour ce faire vous devrez faire un balayage de la droite vers la gauche pour lancer la reconnaissance vocale.

### Déroulement des énigmes

Une fois arrivé dans une destination, l'énigme vous sera alors énoncée. Il existe deux types d'énigmes, les énigmes à reconnaissance vocale et les énigmes à détection de QRCode.

#### Énigme à reconnaissance vocale

Pour résoudre une énigme à reconnaissance vocale, il faudra effectuer un balayage de la droite vers la gauche pour lancer la reconnaissance vocale pour ensuite énoncé la réponse.
Si la réponse est bonne l'énigme sera validé sinon il faudra réessayez.

#### Énigme à détection de QRCode

Pour résoudre une énigme à détection de QRCode, il faudra scanner le QRCode correspondant à la bonne réponse pour valider l'énigme sinon il faudra retenter sa chance.

### Fin du jeu

Le jeu se termine lorsque toutes les énigme sont résolues.